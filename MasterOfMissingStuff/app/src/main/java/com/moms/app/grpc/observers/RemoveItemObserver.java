package com.moms.app.grpc.observers;

import com.moms.app.ItemActivity;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class RemoveItemObserver implements StreamObserver<Communication.Ack> {

    // Private attributes

    private final ItemActivity activity;

    // Constructors

    public RemoveItemObserver(ItemActivity activity) {
        this.activity = activity;
    }

    // Other methods

    @Override
    public void onNext(Communication.Ack value) {
        activity.itemRemoved();
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Error while receiving an Ack from a 'removeItem' call: ");
        t.printStackTrace();
    }

    @Override
    public void onCompleted() {

    }
}
