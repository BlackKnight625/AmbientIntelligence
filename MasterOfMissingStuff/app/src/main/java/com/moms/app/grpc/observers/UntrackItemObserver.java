package com.moms.app.grpc.observers;

import com.moms.app.ItemActivity;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class UntrackItemObserver implements StreamObserver<Communication.Ack> {
    private final ItemActivity activity;

    public UntrackItemObserver(ItemActivity activity) { this.activity = activity; }

    @Override
    public void onNext(Communication.Ack value) {
        //TODO
        activity.setTrackSwitch(false);
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Error while receiving an Ack from a 'untrackItem' call: ");
        t.printStackTrace();
    }

    @Override
    public void onCompleted() {

    }
}
