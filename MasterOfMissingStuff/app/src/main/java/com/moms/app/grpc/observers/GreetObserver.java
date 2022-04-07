package com.moms.app.grpc.observers;

import com.moms.app.MainActivity;
import com.moms.app.grpc.CentralSystemFrontend;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class GreetObserver implements StreamObserver<Communication.Ack> {

    // Private attributes

    private final MainActivity activity;

    // Constructors

    public GreetObserver(MainActivity activity) {
        this.activity = activity;
    }

    // Other methods

    @Override
    public void onNext(Communication.Ack value) {
        CentralSystemFrontend.FRONTEND.communicationEstablished();
        activity.greetingReceived();
    }

    @Override
    public void onError(Throwable t) {
        activity.greetingError(t);
    }

    @Override
    public void onCompleted() {

    }
}
