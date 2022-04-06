package com.moms.app.grpc.observers;

import com.moms.app.AddItemActivity;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class PhotoTakenObserver implements StreamObserver<Communication.PhotoResponse> {

    // Private attributes

    private final AddItemActivity activity;

    // Constructors

    public PhotoTakenObserver(AddItemActivity activity) {
        this.activity = activity;
    }

    // Other methods

    @Override
    public void onNext(Communication.PhotoResponse value) {
        activity.photoTakenResponseReceived(value.getNewItemId().getId(), value.getStatus());
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
