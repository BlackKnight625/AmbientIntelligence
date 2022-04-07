package com.moms.app.grpc.observers;

import java.util.ArrayList;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class StatusResponseObserver implements StreamObserver<Communication.StatusResponse> {

    @Override
    public void onNext(Communication.StatusResponse value) {
        switch (value.getStatus()) {
            case OK:
                //Do nothing
                break;
            case LOCKED_ITEMS_MOVED:
                //TODO: Send notification
                System.out.println("At least 1 item has moved. Item names: " +
                        new ArrayList<>(value.getMovedLockedItems().getItemNamesList()));
                break;
            case CAMERA_TURNED_OFF:
                //For now, do nothing
                break;
        }
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Error while receiving an element of the Status Response stream from a 'statusRequest' call: ");
        t.printStackTrace();
    }

    @Override
    public void onCompleted() {

    }
}
