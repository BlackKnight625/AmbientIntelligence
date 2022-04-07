package com.moms.app.grpc.observers;

import com.moms.app.AddItemActivity;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class ConfirmItemInsertionObserver implements StreamObserver<Communication.Ack> {

    // Private attributes

    private final AddItemActivity activity;

    // Constructors

    public ConfirmItemInsertionObserver(AddItemActivity activity) {
        this.activity = activity;
    }

    // Other methods

    @Override
    public void onNext(Communication.Ack value) {
        //TODO
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Error while receiving an Ack from a 'confirmItemInsertion' call: ");
        t.printStackTrace();
    }

    @Override
    public void onCompleted() {

    }
}
