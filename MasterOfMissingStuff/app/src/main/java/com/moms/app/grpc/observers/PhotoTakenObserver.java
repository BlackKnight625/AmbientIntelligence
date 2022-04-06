package com.moms.app.grpc.observers;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class PhotoTakenObserver implements StreamObserver<Communication.PhotoResponse> {
    @Override
    public void onNext(Communication.PhotoResponse value) {
        onNext(value.getNewItemId().getId(), value.getStatus());
    }

    public void onNext(String newItemId, Communication.PhotoResponse.ResponseStatus status) {
        //TODO

        switch (status) {
            case OK:
                break;
            case UNRECOGNIZED:
                break;
            case NO_ITEM_FOUND:
                break;
            case ITEM_ALREADY_EXISTS:
                break;
            case MULTIPLE_ITEMS_FOUND:
                break;
        }
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
