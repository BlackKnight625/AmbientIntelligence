package com.moms.app.grpc;

import com.moms.app.grpc.observers.ConfirmItemInsertionObserver;
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
import java.util.concurrent.Semaphore;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.moms.grpc.Communication;
import pt.tecnico.moms.grpc.SmartphoneAppToCentralSystemServiceGrpc;

public class CentralSystemFrontend {

    // Private attributes

        //Coms related
    private SmartphoneAppToCentralSystemServiceGrpc.SmartphoneAppToCentralSystemServiceStub stub = null;
    private ManagedChannel channel;
    private Semaphore semaphore = new Semaphore(0);

    // Public attributes

    public static CentralSystemFrontend FRONTEND;

    // Constructors

    public CentralSystemFrontend(String ip, String port, GreetObserver greetObserver) {
        new Thread() {
            @Override
            public void run() {
                try {
                    channel = ManagedChannelBuilder.forAddress(ip, Integer.parseInt(port)).usePlaintext().build();

                    stub = SmartphoneAppToCentralSystemServiceGrpc.newStub(channel);
                    System.out.println("Communication between Central System and Smartphone App established!");
                } catch (NumberFormatException e) {
                    greetObserver.onError(e);
                    return;
                }

                // All future threads that want to use the stub may now do so
                semaphore.release(Integer.MAX_VALUE);

                statusRequest(); //Asking for status requests to be sent every second
            }
        }.start();

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
    }

    // Service methods

    public void greet(GreetObserver observer) {
        waitForLoadedStub();

        stub.greet(Communication.Ack.newBuilder().build(), observer);
    }

    public void statusRequest() {
        waitForLoadedStub();

        stub.statusRequest(Communication.StatusRequest.newBuilder().build(), new StatusResponseObserver());
    }

    private void keepAlive() {
        waitForLoadedStub();

        stub.keepAlive(Communication.Ack.newBuilder().build(), new KeepAliveObserver());
    }

    public void locateItem(String id, LocateItemObserver footageReceivedObserver) {
        waitForLoadedStub();

        Communication.ItemId itemId = getIdFrom(id);

        stub.locateItem(itemId, footageReceivedObserver);
    }

    public void photoTaken(ByteString footageBytes, Calendar currentTime, PhotoTakenObserver photoStatusObserver) {
        waitForLoadedStub();

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

        stub.photoTaken(footage, photoStatusObserver);
    }

    public void confirmItemInsertion(String id, boolean tracked, boolean locked, String itemName, ConfirmItemInsertionObserver itemInsertionAckObserver) {
        waitForLoadedStub();

        Communication.ItemInformation information = Communication.ItemInformation.newBuilder().
                setItemId(getIdFrom(id)).
                setTracked(tracked).
                setLocked(locked).
                setName(itemName).
                setImage(ByteString.EMPTY).
                build();

        stub.confirmItemInsertion(information, itemInsertionAckObserver);
    }

    public void searchItem(String itemName, SearchItemObserver searchedItemsObserver) {
        waitForLoadedStub();

        Communication.SearchParameters parameters = Communication.SearchParameters.newBuilder().
                setItemName(itemName).
                build();

        stub.searchItem(parameters, searchedItemsObserver);
    }

    public void trackItem(String id, TrackItemObserver trackingItemAckObserver) {
        waitForLoadedStub();

        stub.trackItem(getIdFrom(id), trackingItemAckObserver);
    }

    public void untrackItem(String id, UntrackItemObserver untrackingItemAckObserver) {
        waitForLoadedStub();

        stub.untrackItem(getIdFrom(id), untrackingItemAckObserver);
    }

    public void lockItem(String id, LockItemObserver lockingItemAckObserver) {
        waitForLoadedStub();

        stub.lockItem(getIdFrom(id), lockingItemAckObserver);
    }

    public void unlockItem(String id, UnlockItemObserver unlockingItemAckObserver) {
        waitForLoadedStub();

        stub.unlockItem(getIdFrom(id), unlockingItemAckObserver);
    }

    public void removeItem(String id, RemoveItemObserver removingItemAckObserver) {
        waitForLoadedStub();

        stub.removeItem(getIdFrom(id), removingItemAckObserver);
    }

    // Other methods

    public void waitForLoadedStub() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }
    }

    public Communication.ItemId getIdFrom(String id) {
        return Communication.ItemId.newBuilder()
                .setId(id)
                .build();
    }
}
