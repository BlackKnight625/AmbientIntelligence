package com.moms.app;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.moms.app.grpc.CentralSystemFrontend;
import com.moms.app.grpc.observers.GreetObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;

public class MainActivity extends AppCompatActivity {
    public static HashMap<String, Item> ITEMS;
    public static boolean REQUEST_LOAD_ITEMS = true;
    public static int REQUEST_REMOVE = 0;
    public static int RESULT_OK = 0;

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
            showPopupWindow(this, error.getMessage());
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

    public static void showPopupWindow(Activity activity, String popupText) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                activity.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.error_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(activity.getCurrentFocus(), Gravity.CENTER, 0, 0);

        TextView errorPopupText = popupView.findViewById(R.id.errorPopupText);

        errorPopupText.setText(popupText);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }
}