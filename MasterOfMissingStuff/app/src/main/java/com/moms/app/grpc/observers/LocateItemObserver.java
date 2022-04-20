package com.moms.app.grpc.observers;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.moms.app.ItemActivity;

import java.util.List;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class LocateItemObserver implements StreamObserver<Communication.VideoFootage> {

    // Private attributes

    private final ItemActivity activity;

    // Constructors

    public LocateItemObserver(ItemActivity activity) {
        this.activity = activity;
    }

    // Other methods

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onNext(Communication.VideoFootage value) {
        activity.itemLocated(value.getFootageSize());
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
