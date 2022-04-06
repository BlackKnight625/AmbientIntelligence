package com.moms.app;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.moms.app.grpc.CentralSystemFrontend;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.insertip);

        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this,R.color.white));// set status background white

        final Button next_layout_button = findViewById(R.id.button8);
        next_layout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Code here executes on main thread after user presses button
                final EditText ip_text =  (EditText) findViewById(R.id.editTextTextPersonName2);
                final EditText port_text = (EditText) findViewById(R.id.editTextTextPersonName3);

                CentralSystemFrontend.FRONTEND = new CentralSystemFrontend(ip_text.getText().toString(), port_text.getText().toString());

                //CSFrontend = new CentralSystemFrontend(ip_text.getText().toString(), Integer.parseInt(port_text.getText().toString()));
                startActivity(new Intent(getApplicationContext(), AddSearchActivity.class));
            }
        });
    }
}