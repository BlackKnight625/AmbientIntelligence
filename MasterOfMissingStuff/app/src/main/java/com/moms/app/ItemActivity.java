package com.moms.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.moms.app.grpc.CentralSystemFrontend;
import com.moms.app.grpc.observers.LocateItemObserver;
import com.moms.app.grpc.observers.LockItemObserver;
import com.moms.app.grpc.observers.RemoveItemObserver;
import com.moms.app.grpc.observers.TrackItemObserver;
import com.moms.app.grpc.observers.UnlockItemObserver;
import com.moms.app.grpc.observers.UntrackItemObserver;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import pt.tecnico.moms.grpc.Communication;

public class ItemActivity extends AppCompatActivity {

    // Private attributes

    private Switch track_switch;
    private Switch lock_switch;
    private Item item;

    // Other methods

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item);

        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(ItemActivity.this,R.color.white));// set status background white

        String idName = getIntent().getExtras().getString("item");
        item = MainActivity.ITEMS.get(idName);

        TextView textView = (TextView) findViewById(R.id.textView5);
        textView.setText(item.getName());

        final Button back_button = findViewById(R.id.button11);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Code here executes on main thread after user presses button
                finish();
            }
        });

        final Button remove_button = findViewById(R.id.button2);
        remove_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Code here executes on main thread after user presses button
                //startActivity(new Intent(ItemActivity.this, AddItemActivity.class));

                CentralSystemFrontend.FRONTEND.removeItem(item.getIdName(), new RemoveItemObserver(ItemActivity.this));
            }
        });

        final Button locate_button = findViewById(R.id.button);
        locate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Code here executes on main thread after user presses button
                //startActivity(new Intent(ItemActivity.this, AddItemActivity.class));

                CentralSystemFrontend.FRONTEND.locateItem(item.getIdName(), new LocateItemObserver(ItemActivity.this));
            }
        });

        lock_switch = findViewById(R.id.switch1);
        lock_switch.setChecked(item.isLocked());
        lock_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                // Code here executes on main thread after user presses switch
                System.out.println("Lock is: " + isChecked);

                if(isChecked) {
                    CentralSystemFrontend.FRONTEND.lockItem(item.getIdName(), new LockItemObserver(ItemActivity.this));
                }
                else {
                    CentralSystemFrontend.FRONTEND.unlockItem(item.getIdName(), new UnlockItemObserver(ItemActivity.this));
                }
            }
        });

        track_switch = findViewById(R.id.switch2);
        track_switch.setChecked(item.isTracked());
        track_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                // Code here executes on main thread after user presses switch
                System.out.println("Track is: " + isChecked);

                if(isChecked) {
                    CentralSystemFrontend.FRONTEND.trackItem(item.getIdName(), new TrackItemObserver(ItemActivity.this));
                }
                else {
                    CentralSystemFrontend.FRONTEND.untrackItem(item.getIdName(), new UntrackItemObserver(ItemActivity.this));
                }
            }
        });
    }

    /**
     * Called when an Ack is received after removing an item
     */
    public void itemRemoved() {
        MainActivity.ITEMS.remove(item.getIdName());
        setResult(MainActivity.RESULT_OK);
        finish();
    }

    /**
     *  Called when footage and bounding boxes are received after locating an item
     * @param pictures
     *  List of pictures corresponding to the last 5 seconds of when the item was last seen
     * @param boundingBoxes
     *  List of Bounding boxes associated with each picture that show where the item is
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void itemLocated(List<Communication.Footage> pictures, List<Communication.BoundingBox> boundingBoxes) {
        if(pictures.isEmpty()) {
            runOnUiThread(() -> {
                MainActivity.showPopupWindow(ItemActivity.this, "There's no footage of this item", findViewById(R.id.imageView3));
            });
        }
        else {
            runOnUiThread(() -> {
                //Creating the view to show the footage

                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater)
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.locate_footage, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                popupWindow.showAtLocation(findViewById(R.id.imageView3), Gravity.CENTER, 0, 0);

                ImageView imageView = popupView.findViewById(R.id.locateFootageView);

                // dismiss the popup window when touched
                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });

                //Creating a gif with the pictures

                //Converting all picture's ByteStrings to Bitmaps
                List<Bitmap> bitmaps = pictures.
                        stream().
                        map(f -> f.getPicture()).
                        map(bs -> {
                            byte[] byteArray = bs.toByteArray();
                            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        }).
                        collect(Collectors.toList());

                Paint paint=new Paint();
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5);

                //Modifying the bitmaps to have rectangles drawn on them
                for(int i = 0; i < bitmaps.size(); i++) {
                    Bitmap bitmap = bitmaps.get(i);

                    Bitmap newBitmap = bitmap.copy(bitmap.getConfig(), true);

                    bitmaps.set(i, newBitmap);

                    Canvas canvas = new Canvas(newBitmap);
                    Communication.BoundingBox box = boundingBoxes.get(i);

                    canvas.drawRect(box.getLow().getX(), box.getHigh().getY(), box.getHigh().getX(), box.getLow().getY(), paint);
                }

                AtomicBoolean stopShowing = new AtomicBoolean(false);

                new Thread() {
                    int i = 0;

                    @Override
                    public void run() {
                        while(!stopShowing.get()) {
                            runOnUiThread(() -> {
                                int index = i % bitmaps.size();

                                Bitmap bitmap = bitmaps.get(index);

                                imageView.setImageBitmap(bitmap);
                            });

                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            i++;
                        }
                    }
                }.start();

                popupWindow.setOnDismissListener(() -> stopShowing.set(true));
            });
        }
    }

    public void setTrackSwitch(boolean track) {
        runOnUiThread(() -> track_switch.setChecked(track));
        item.setTrack(track);
    }

    public void setLockSwitch(boolean lock) {
        runOnUiThread(() -> lock_switch.setChecked(lock));
        item.setLock(lock);
    }
}
