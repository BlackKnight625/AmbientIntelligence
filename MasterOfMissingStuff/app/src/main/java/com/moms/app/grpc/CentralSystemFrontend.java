package com.moms.app.grpc;

import com.moms.app.grpc.observers.FrameObserver;
import com.moms.app.grpc.observers.GreetObserver;
import com.moms.app.grpc.observers.KeepAliveObserver;
import com.moms.app.grpc.observers.LocateItemObserver;
import com.moms.app.grpc.observers.LockItemObserver;
import com.moms.app.grpc.observers.PhotoTakenObserver;
import com.moms.app.grpc.observers.RemoveItemObserver;
import com.moms.app.grpc.observers.SearchItemObserver;
import com.moms.app.grpc.observers.StatusResponseObserver;
import com.moms.app.grpc.observers.TrackItemObserver;
import com.moms.app.grpc.observers.UnlockItemObserver;
import com.moms.app.grpc.observers.UntrackItemObserver;
import com.google.protobuf.ByteString;

import java.util.Calendar;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.moms.grpc.Communication;
import pt.tecnico.moms.grpc.SmartphoneAppToCentralSystemServiceGrpc;

public class CentralSystemFrontend {

    // Private attributes

        //Coms related
    private SmartphoneAppToCentralSystemServiceGrpc.SmartphoneAppToCentralSystemServiceStub stub = null;
    private ManagedChannel channel;

    // Public attributes

    public static CentralSystemFrontend FRONTEND;

    // Constructors

    public CentralSystemFrontend(String ip, String port) throws NumberFormatException {
        try {
            channel = ManagedChannelBuilder.forAddress(ip, Integer.parseInt(port)).usePlaintext().build();

            stub = SmartphoneAppToCentralSystemServiceGrpc.newStub(channel);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Given port (" + port + ") is not a valid por number.");
        }
    }

    // Service methods

    public void greet(GreetObserver observer) {
        stub.greet(Communication.Ack.newBuilder().build(), observer);
    }

    public void statusRequest() {
        stub.statusRequest(Communication.StatusRequest.newBuilder().build(), new StatusResponseObserver());
    }

    private void keepAlive() {
        stub.keepAlive(Communication.Ack.newBuilder().build(), new KeepAliveObserver());
    }

    public void locateItem(String id, LocateItemObserver footageReceivedObserver) {
        Communication.ItemId itemId = getIdFrom(id);

        stub.locateItem(itemId, footageReceivedObserver);
    }

    public void nextFrame(FrameObserver frameObserver) {
        stub.nextFrame(Communication.FrameRequest.newBuilder().build(), frameObserver);
    }

    public void photoTaken(ByteString footageBytes, String itemName, Calendar currentTime, PhotoTakenObserver photoStatusObserver) {
        //Note: Calendar.getInstance() returns a Calendar with the current timestamp

        Communication.Timestamp timestamp = Communication.Timestamp.newBuilder().
                setSeconds(currentTime.get(Calendar.SECOND)).
                setMinutes(currentTime.get(Calendar.MINUTE)).
                setHour(currentTime.get(Calendar.HOUR)).
                setDay(currentTime.get(Calendar.DAY_OF_MONTH)).
                setMonth(currentTime.get(Calendar.MONTH)).
                setYear(currentTime.get(Calendar.YEAR)).
                build();

        Communication.Footage footage = Communication.Footage.newBuilder().
                setPicture(footageBytes).
                setTime(timestamp).
                build();

        Communication.PhotoRequest request = Communication.PhotoRequest.newBuilder().
                setItemName(itemName).
                setFootage(footage).
                build();

        stub.photoTaken(request, photoStatusObserver);
    }

    public void searchItem(String itemName, SearchItemObserver searchedItemsObserver) {
        Communication.SearchParameters parameters = Communication.SearchParameters.newBuilder().
                setItemName(itemName).
                build();

        stub.searchItem(parameters, searchedItemsObserver);
    }

    public void trackItem(String id, TrackItemObserver trackingItemAckObserver) {
        stub.trackItem(getIdFrom(id), trackingItemAckObserver);
    }

    public void untrackItem(String id, UntrackItemObserver untrackingItemAckObserver) {
        stub.untrackItem(getIdFrom(id), untrackingItemAckObserver);
    }

    public void lockItem(String id, LockItemObserver lockingItemAckObserver) {
        stub.lockItem(getIdFrom(id), lockingItemAckObserver);
    }

    public void unlockItem(String id, UnlockItemObserver unlockingItemAckObserver) {
        stub.unlockItem(getIdFrom(id), unlockingItemAckObserver);
    }

    public void removeItem(String id, RemoveItemObserver removingItemAckObserver) {
        stub.removeItem(getIdFrom(id), removingItemAckObserver);
    }

    // Other methods

    public Communication.ItemId getIdFrom(String id) {
        return Communication.ItemId.newBuilder()
                .setId(id)
                .build();
    }

    /**
     *  Called by the GreetObserver when a connection is established
     */
    public void communicationEstablished() {
        statusRequest(); //Asking for status requests to be sent every second

        new Thread() {
            @Override
            public void run() {
                while(true) {
                    try {
                        keepAlive();
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        System.out.println("Communication between Central System and Smartphone App established!");
    }
}
