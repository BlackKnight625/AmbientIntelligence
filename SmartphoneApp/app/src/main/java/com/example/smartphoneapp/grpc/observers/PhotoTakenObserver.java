package com.example.smartphoneapp.grpc.observers;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

import static pt.tecnico.moms.grpc.Communication.PhotoResponse.ResponseStatus.*;
import static pt.tecnico.moms.grpc.Communication.PhotoResponse.ResponseStatus;

public class PhotoTakenObserver implements StreamObserver<Communication.PhotoResponse> {
    @Override
    public void onNext(Communication.PhotoResponse value) {
        onNext(value.getNewItemId().getId(), value.getStatus());
    }

    public void onNext(String newItemId, ResponseStatus status) {
        //TODO
        //Cool thing: thanks to one of the imports I did, it's possible to write (status == NO_ITEM_FOUND) instead of
        // (status == Communication.PhotoResponse.ResponseStatus.NO_ITEMS_FOUND)
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Error while receiving a response from a 'photoTaken' call: ");
        t.printStackTrace();
    }

    @Override
    public void onCompleted() {

    }
}
