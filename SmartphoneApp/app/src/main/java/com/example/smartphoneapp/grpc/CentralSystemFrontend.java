package com.example.smartphoneapp.grpc;

import com.example.smartphoneapp.grpc.observers.LocateItemObserver;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.moms.grpc.CameraToCentralSystemServiceGrpc;
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
