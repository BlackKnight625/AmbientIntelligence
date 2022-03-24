package com.example.cameraapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.widget.FrameLayout;

import java.io.IOException;
import java.net.Socket;

public class SurveilanceActivity extends AppCompatActivity {

    // Private attributes

    private Socket socket;
    private Camera camera;
    private CameraPreview preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surveilance);

        if(safeCameraOpen()) {
            preview = new CameraPreview(getApplicationContext(), camera);
            ((FrameLayout) findViewById(R.id.cameraPreview)).addView(preview);
        }

        //Thread that creates the socket
        /*new Thread() {
            @Override
            public void run() {
                try {
                    //Creating the socket
                    socket = new Socket(getIntent().getStringExtra(MainActivity.IP_MESSAGE),
                            Integer.parseInt(getIntent().getStringExtra(MainActivity.PORT_MESSAGE)));
                } catch (IOException e) {
                    //Something went wrong when creating the socket
                    errorSendToMainScreen(getString(R.string.error) + " " + e.getMessage(), e);
                } catch (NumberFormatException e) {
                    //The port was not well formatted
                    errorSendToMainScreen(getString(R.string.error) + " " + getString(R.string.wrong_format) + " " + e.getMessage(), e);
                }
            }
        }.start();*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if(socket != null) {
                socket.close();
            }

            releaseCamera();
        } catch (IOException e) {
            e.printStackTrace();
        }
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