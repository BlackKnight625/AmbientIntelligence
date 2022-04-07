package com.moms.app;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.moms.app.grpc.CentralSystemFrontend;
import com.moms.app.grpc.observers.GreetObserver;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.insertip);

        //This is a dark magic line that makes some things not explode, for some reason
        InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE);

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

                GreetObserver observer = new GreetObserver(MainActivity.this);

                try {
                    CentralSystemFrontend.FRONTEND = new CentralSystemFrontend(ip_text.getText().toString(), port_text.getText().toString());

                    CentralSystemFrontend.FRONTEND.greet(observer);
                } catch (NumberFormatException e) {
                    greetingError(e);
                }
            }
        });
    }

    /**
     *  Called when the greeting from this App receives a greeting response, meaning
     * that the connection with the Central System was established with success.
     */
    public void greetingReceived() {
        startActivity(new Intent(getApplicationContext(), AddSearchActivity.class));
    }

    /**
     *  Called when the greeting from this App returns with an error, meaning
     * something went wrong while trying to connect to the Central System
     * @param error
     *  The error that prevented a connection from being established
     */
    public void greetingError(Throwable error) {
        //TODO: Show the user what went wrong
        runOnUiThread(() -> {
            TextView errorTextView = findViewById(R.id.errorTextView);

            errorTextView.setText(error.getMessage());
        });
    }
}