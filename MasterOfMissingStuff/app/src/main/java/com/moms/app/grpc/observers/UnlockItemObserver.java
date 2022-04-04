package com.moms.app.grpc.observers;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class UnlockItemObserver implements StreamObserver<Communication.Ack> {

    @Override
    public void onNext(Communication.Ack value) {
        //TODO
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Error while receiving an Ack from a 'unlockItem' call: ");
        t.printStackTrace();
    }

    @Override
    public void onCompleted() {

    }
}
