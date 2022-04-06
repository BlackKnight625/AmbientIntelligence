package com.moms.app.grpc.observers;

import java.util.List;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class SearchItemObserver implements StreamObserver<Communication.SearchResponse> {

    @Override
    public void onNext(Communication.SearchResponse value) {
        onNext(value.getSearchResultsList());
    }

    public void onNext(List<Communication.ItemInformation> itemInformations) {
        //TODO
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Error while receiving an Items Information from a 'locateItem' call: ");
        t.printStackTrace();
    }

    @Override
    public void onCompleted() {

    }
}
