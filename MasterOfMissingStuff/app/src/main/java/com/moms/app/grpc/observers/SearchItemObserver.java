package com.moms.app.grpc.observers;

import com.moms.app.MyItemsActivity;
import com.moms.app.SearchCompatible;

import java.util.List;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class SearchItemObserver implements StreamObserver<Communication.SearchResponse> {

    // Private attributes

    private final SearchCompatible activity;

    // Constructors

    public SearchItemObserver(SearchCompatible activity) {
        this.activity = activity;
    }


    @Override
    public void onNext(Communication.SearchResponse value) {
        activity.searchedItems(value.getSearchResultsList());
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Error while receiving an Items Information from a 'searchItem' call: ");
        t.printStackTrace();
    }

    @Override
    public void onCompleted() {

    }
}
