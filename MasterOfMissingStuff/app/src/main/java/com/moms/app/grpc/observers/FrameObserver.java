package com.moms.app.grpc.observers;

import com.moms.app.ItemActivity;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class FrameObserver implements StreamObserver<Communication.Frame> {

    // Private attributes

    private final ItemActivity activity;

    // Constructors

    public FrameObserver(ItemActivity activity) {
        this.activity = activity;
    }

    // Other methods

    @Override
    public void onNext(Communication.Frame frame) {
        activity.newFrame(frame.getPicture(), frame.getBox());
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Error while receiving Footage from a 'nextFrame' call: ");
        t.printStackTrace();
    }

    @Override
    public void onCompleted() {

    }
}
