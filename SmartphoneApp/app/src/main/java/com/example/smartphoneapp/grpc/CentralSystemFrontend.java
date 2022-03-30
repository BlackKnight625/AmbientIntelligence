package com.example.smartphoneapp.grpc;

import com.example.smartphoneapp.grpc.observers.ConfirmItemInsertionObserver;
import com.example.smartphoneapp.grpc.observers.LocateItemObserver;
import com.example.smartphoneapp.grpc.observers.LockItemObserver;
import com.example.smartphoneapp.grpc.observers.PhotoTakenObserver;
import com.example.smartphoneapp.grpc.observers.RemoveItemObserver;
import com.example.smartphoneapp.grpc.observers.SearchItemObserver;
import com.example.smartphoneapp.grpc.observers.TrackItemObserver;
import com.example.smartphoneapp.grpc.observers.UnlockItemObserver;
import com.example.smartphoneapp.grpc.observers.UntrackItemObserver;
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
    private int timeout = 2000; //2 seconds timeout for messages
    private Semaphore semaphore = new Semaphore(0);

    // Public attributes

    public static CentralSystemFrontend FRONTEND;

    // Constructors

    public CentralSystemFrontend(String ip, int port) {
        new Thread() {
            @Override
            public void run() {
                channel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext().build();

                stub = SmartphoneAppToCentralSystemServiceGrpc.newStub(channel);

                // All future threads that want to use the stub may now do so
                semaphore.release(Integer.MAX_VALUE);
            }
        }.start();
    }

    // Service methods

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

    public void confirmItemInsertion(String id, ConfirmItemInsertionObserver itemInsertionAckObserver) {
        waitForLoadedStub();

        stub.confirmItemInsertion(getIdFrom(id), itemInsertionAckObserver);
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
        }
    }

    public Communication.ItemId getIdFrom(String id) {
        return Communication.ItemId.newBuilder()
                .setId(id)
                .build();
    }
}
