package com.moms.app.grpc.observers;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class GreetObserver implements StreamObserver<Communication.Ack> {
    @Override
    public void onNext(Communication.Ack value) {
        // Do nothing
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Error while receiving Ack from a 'greet' call: ");
        t.printStackTrace();
    }

    @Override
    public void onCompleted() {

    }
}
