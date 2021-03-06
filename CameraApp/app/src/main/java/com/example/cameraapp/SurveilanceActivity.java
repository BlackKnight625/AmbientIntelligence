package com.example.cameraapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.concurrent.Semaphore;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.moms.grpc.CameraToCentralSystemServiceGrpc;
import pt.tecnico.moms.grpc.Communication;

public class SurveilanceActivity extends AppCompatActivity {

    // Private attributes

        //Android related
    private Camera camera;
    private CameraPreview preview;
    MediaPlayer photoSound;

        //Grpc related
    private ManagedChannel channel;
    private CameraToCentralSystemServiceGrpc.CameraToCentralSystemServiceStub stub = null;

        //Others
    private boolean destroyed = false;
    private Semaphore semaphore = new Semaphore(1);
    private int totalPhotos = 0;
    private long startedMillis;

    // Other methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surveilance);

        startedMillis = System.currentTimeMillis();

        if(safeCameraOpen()) {
            preview = new CameraPreview(getApplicationContext(), camera);
            ((FrameLayout) findViewById(R.id.cameraPreview)).addView(preview);

            photoSound = MediaPlayer.create(getApplicationContext(), R.raw.camera_shutter_click_01);

            //Thread that creates the connection
            new Thread() {
                @Override
                public void run() {
                    try {
                        //Creating the communications
                        channel = ManagedChannelBuilder.forAddress(getIntent().getStringExtra(MainActivity.IP_MESSAGE),
                                Integer.parseInt(getIntent().getStringExtra(MainActivity.PORT_MESSAGE))).usePlaintext().build();

                        stub = CameraToCentralSystemServiceGrpc.newStub(channel);

                    } catch (NumberFormatException e) {
                        //The port was not well formatted
                        errorSendToMainScreen(getString(R.string.error) + " " + getString(R.string.wrong_format) + " " + e.getMessage(), e);
                    } catch (Exception e) {
                        //Something went wrong when creating the socket
                        errorSendToMainScreen(getString(R.string.error) + " " + e.getMessage(), e);
                    }
                }
            }.start();

            //Thread that sends pictures to the central system
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    while(!destroyed) {
                        //Sleeping
                        try {
                            Thread.sleep(250);

                            semaphore.acquire();

                            camera.takePicture(null, null, SurveilanceActivity.this::onPictureTaken);
                            photoSound.start();

                            semaphore.acquire(); //Waiting for the "onPictureTaken" thread to finish

                            semaphore.release();

                            camera.startPreview();

                            totalPhotos++;

                            System.out.println("Photos per second: " + (totalPhotos / ((System.currentTimeMillis() - startedMillis) / 1000.0)));
                        } catch (InterruptedException e) {
                            errorSendToMainScreen(e.getMessage(), e);
                        }
                    }
                }
            }.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(channel != null) {
            channel.shutdown();
        }

        releaseCamera();

        destroyed = true;
    }

    private boolean safeCameraOpen() {
        boolean qOpened = false;

        try {
            releaseCamera();
            camera = Camera.open();
            camera.setDisplayOrientation(90);
            qOpened = (camera != null);
        } catch (Exception e) {
            errorSendToMainScreen(getString(R.string.error_open_camera) + " " + e.getMessage(), e);
        }

        return qOpened;
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private void onPictureTaken(byte[] bytes, Camera camera) {
        if(stub != null) {
            Calendar currentTime = Calendar.getInstance();

            System.out.println("Took picture of size: " + bytes.length);

            Bitmap array = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            array.compress(Bitmap.CompressFormat.JPEG, 50, out);

            byte[] compressedPicture = out.toByteArray();

            System.out.println("Compressed picture to size: " + compressedPicture.length);

            Communication.Timestamp timestamp = Communication.Timestamp.newBuilder().
                    setSeconds(currentTime.get(Calendar.SECOND)).
                    setMinutes(currentTime.get(Calendar.MINUTE)).
                    setHour(currentTime.get(Calendar.HOUR)).
                    setDay(currentTime.get(Calendar.DAY_OF_MONTH)).
                    setMonth(currentTime.get(Calendar.MONTH)).
                    setYear(currentTime.get(Calendar.YEAR)).
                    build();

            Communication.Footage footage = Communication.Footage.newBuilder().
                    setPicture(ByteString.copyFrom(compressedPicture)).
                    setTime(timestamp).
                    build();

            //Sending the footage and creating a new observer that knows the length of the sent footage
            stub.sendFootage(footage, new FootageSendObserver(compressedPicture.length));
        }

        semaphore.release();
    }

    private void errorSendToMainScreen(String errorText, Exception e) {
        runOnUiThread(() -> {
            Intent intent = new Intent(SurveilanceActivity.this, MainActivity.class);

            intent.putExtra(MainActivity.ERROR_MESSAGE, errorText);

            startActivity(intent);

            if(e != null) {
                e.printStackTrace();
            }
        });
    }


}