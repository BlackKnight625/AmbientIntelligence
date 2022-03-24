package com.example.cameraapp;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class FootageSendObserver implements StreamObserver<Communication.FootageAck> {

    // Private attributes

    private int size;

    // Constructors

    public FootageSendObserver(int size) {
        this.size = size;
    }

    // Other methods

    @Override
    public void onNext(Communication.FootageAck value) {
        System.out.println("Footage of size: " + size + " has been received by the central system!");
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Footage of size: " + size + " has not been received by the central system...");
        t.printStackTrace();
    }

    @Override
    public void onCompleted() {

    }
}
