package com.moms.app.grpc.observers;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.protobuf.ByteString;

import java.util.List;
import java.util.stream.Collectors;

import io.grpc.stub.StreamObserver;
import pt.tecnico.moms.grpc.Communication;

public class LocateItemObserver implements StreamObserver<Communication.VideoFootage> {

    @Override
    public void onNext(Communication.VideoFootage value) {
        onNext(value.getPicturesList(), value.getItemBoundingBoxesList());
    }

    public void onNext(List<Communication.Footage> pictures, List<Communication.BoundingBox> boundingBoxes) {
        //TODO
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<ByteString> getBytesFromPictures(List<Communication.Footage> pictures) {
        return pictures.stream().map(footage -> footage.getPicture()).collect(Collectors.toList());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<Communication.Timestamp> getTimestampsFromPictures(List<Communication.Footage> pictures) {
        return pictures.stream().map(footage -> footage.getTime()).collect(Collectors.toList());
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
