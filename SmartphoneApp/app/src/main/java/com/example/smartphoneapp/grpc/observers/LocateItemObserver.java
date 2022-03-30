package com.example.smartphoneapp.grpc.observers;

import java.util.List;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class LocateItemObserver implements StreamObserver<Communication.VideoFootage> {

    @Override
    public void onNext(Communication.VideoFootage value) {
        onNext(value.getPicturesList());
    }

    public void onNext(List<Communication.Footage> pictures) {
        //TODO
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
