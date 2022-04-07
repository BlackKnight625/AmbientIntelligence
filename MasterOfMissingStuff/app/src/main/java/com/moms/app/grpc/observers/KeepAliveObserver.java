package com.moms.app.grpc.observers;

import com.moms.app.grpc.CentralSystemFrontend;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class KeepAliveObserver implements StreamObserver<Communication.KeepAliveResponse> {

    @Override
    public void onNext(Communication.KeepAliveResponse value) {
        switch (value.getStatus()) {
            case OK:
                //Do nothing
                break;
            case SYSTEM_STOPPED_SENDING_STATUS_RESPONSES:
                //Must make the Central System send status requests again
                CentralSystemFrontend.FRONTEND.statusRequest();
                break;
        }
    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onCompleted() {

    }
}
