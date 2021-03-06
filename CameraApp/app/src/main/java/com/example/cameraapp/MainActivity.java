package com.example.cameraapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;

public class MainActivity extends AppCompatActivity {

    public static final String IP_MESSAGE = "pt.tecnico.cameraapp.IP_MESSAGE";
    public static final String PORT_MESSAGE = "pt.tecnico.cameraapp.PORT_MESSAGE";
    public static final String ERROR_MESSAGE = "pt.tecnico.cameraapp.ERROR_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //This is a dark magic line that makes some things not explode, for some reason
        InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE);

        Intent intent;

        if((intent = getIntent()) != null) {
            String error;

            if((error = intent.getStringExtra(ERROR_MESSAGE)) != null) {
                ((TextView) findViewById(R.id.errorView)).setText(error);
            }
        }

        System.out.println("Camera App active!");

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 100);
        }
    }

    public void sendIPAndPort(View view) {
        Intent intent = new Intent(this, SurveilanceActivity.class);

        intent.putExtra(IP_MESSAGE, ((EditText) findViewById(R.id.editIP)).getText().toString());
        intent.putExtra(PORT_MESSAGE, ((EditText) findViewById(R.id.editPort)).getText().toString());

        startActivity(intent);
    }
}