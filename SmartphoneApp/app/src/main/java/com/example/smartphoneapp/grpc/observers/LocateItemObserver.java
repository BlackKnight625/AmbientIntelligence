package com.example.smartphoneapp.grpc.observers;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class LocateItemObserver implements StreamObserver<Communication.VideoFootage> {

    @Override
    public void onNext(Communication.VideoFootage value) {
        onNext();
        //TODO
    }

    public void onNext() {

    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Error while receiving Footage from a 'locateItem' call: ");
        t.printStackTrace();
    }

    @Override
    public void onCompleted() {

    }
}
