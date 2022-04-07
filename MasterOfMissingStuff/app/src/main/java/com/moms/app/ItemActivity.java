package com.moms.app;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

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

import pt.tecnico.moms.grpc.Communication;

public class ItemActivity extends AppCompatActivity {

    // Private attributes

    private Communication.ItemInformation itemInformation = null; //TODO must be set on onCreate

    // Other methods

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item);

        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(ItemActivity.this,R.color.white));// set status background white

        Item item = getIntent().getParcelableExtra("item");
        System.out.println(item.getName());

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

                CentralSystemFrontend.FRONTEND.removeItem(itemInformation.getItemId().getId(), new RemoveItemObserver(ItemActivity.this));
            }
        });

        final Button locate_button = findViewById(R.id.button);
        locate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Code here executes on main thread after user presses button
                //startActivity(new Intent(ItemActivity.this, AddItemActivity.class));

                CentralSystemFrontend.FRONTEND.locateItem(itemInformation.getItemId().getId(), new LocateItemObserver(ItemActivity.this));
            }
        });

        final Switch lock_switch = findViewById(R.id.switch1);
        lock_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                // Code here executes on main thread after user presses switch
                System.out.println("Lock is: " + isChecked);

                if(isChecked) {
                    CentralSystemFrontend.FRONTEND.lockItem(itemInformation.getItemId().getId(), new LockItemObserver());
                }
                else {
                    CentralSystemFrontend.FRONTEND.unlockItem(itemInformation.getItemId().getId(), new UnlockItemObserver());
                }
            }
        });

        final Switch track_switch = findViewById(R.id.switch2);
        track_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                // Code here executes on main thread after user presses switch
                System.out.println("Track is: " + isChecked);

                if(isChecked) {
                    CentralSystemFrontend.FRONTEND.trackItem(itemInformation.getItemId().getId(), new TrackItemObserver());
                }
                else {
                    CentralSystemFrontend.FRONTEND.untrackItem(itemInformation.getItemId().getId(), new UntrackItemObserver());
                }
            }
        });
    }

    /**
     * Called when an Ack is received after removing an item
     */
    public void itemRemoved() {
        //TODO
    }

    /**
     *  Called when footage and bounding boxes are received after locating an item
     * @param pictures
     *  List of pictures corresponding to the last 5 seconds of when the item was last seen
     * @param boundingBoxes
     *  List of Bounding boxes associated with each picture that show where the item is
     */
    public void itemLocated(List<Communication.Footage> pictures, List<Communication.BoundingBox> boundingBoxes) {
        //TODO
    }
}
