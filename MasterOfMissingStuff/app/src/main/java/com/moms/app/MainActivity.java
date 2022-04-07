package com.moms.app;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
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

    public static MainActivity MAIN_ACTIVITY;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.insertip);

        createNotificationChannel();

        MAIN_ACTIVITY = this;

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

        //Testing notifications
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendNotification("Hello there!", "General Kenobi");
            }
        }.start();
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
        runOnUiThread(() -> {
            TextView errorTextView = findViewById(R.id.errorTextView);

            errorTextView.setText(error.getMessage());
        });
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notifications";
            String description = "For this Apps notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Channel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private static int i = 0;
    public static void sendNotification(String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.MAIN_ACTIVITY.getApplicationContext(),
                "Channel")
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.exclamation)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        MAIN_ACTIVITY.runOnUiThread(() -> {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(
                    MAIN_ACTIVITY.getApplicationContext());

            notificationManager.notify(i++, builder.build());
        });
    }
}