package com.example.cameraapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import java.io.IOException;
import java.net.Socket;

public class SurveilanceActivity extends AppCompatActivity {

    // Private attributes

    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surveilance);

        //Thread that creates the socket
        new Thread() {
            @Override
            public void run() {
                try {
                    socket = new Socket(getIntent().getStringExtra(MainActivity.IP_MESSAGE),
                            Integer.parseInt(getIntent().getStringExtra(MainActivity.PORT_MESSAGE)));
                } catch (IOException e) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(SurveilanceActivity.this, MainActivity.class);

                        intent.putExtra(MainActivity.ERROR_MESSAGE,
                                getString(R.string.error) + " " + e.getMessage());

                        startActivity(intent);
                    });
                } catch (NumberFormatException e) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(SurveilanceActivity.this, MainActivity.class);

                        intent.putExtra(MainActivity.ERROR_MESSAGE,
                                getString(R.string.error) + " " + getString(R.string.wrong_format) + " " + e.getMessage());

                        startActivity(intent);
                    });
                }
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}