package com.moms.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.moms.app.grpc.CentralSystemFrontend;
import com.moms.app.grpc.observers.SearchItemObserver;

import java.util.HashMap;
import java.util.List;

import pt.tecnico.moms.grpc.Communication;

public class AddSearchActivity extends AppCompatActivity implements SearchCompatible {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addsearch);

        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(AddSearchActivity.this,R.color.white));// set status background white

        if (MainActivity.REQUEST_LOAD_ITEMS) {
            CentralSystemFrontend.FRONTEND.searchItem("", new SearchItemObserver(this));
        }

        final Button add_item_button = findViewById(R.id.button4);
        add_item_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Code here executes on main thread after user presses button
                startActivity(new Intent(getApplicationContext(), AddItemActivity.class));
            }
        });

        final Button search_button = findViewById(R.id.button6);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Code here executes on main thread after user presses button
                startActivity(new Intent(getApplicationContext(), MyItemsActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed () {

    }

    @Override
    public void searchedItems(List<Communication.ItemInformation> itemsList) {
        MainActivity.ITEMS = new HashMap<>();
        for (int i = 0; i < itemsList.size(); i++) {
            String idName = itemsList.get(i).getItemId().getId();
            byte[] imageBytes = itemsList.get(i).getImage().toByteArray();
            Bitmap image = BitmapFactory.decodeByteArray(imageBytes , 0, imageBytes .length);
            String name = itemsList.get(i).getName();
            boolean lock = itemsList.get(i).getLocked();
            boolean track = itemsList.get(i).getTracked();

            MainActivity.ITEMS.put(idName, new Item(name, image, track, lock, idName));
        }
        MainActivity.REQUEST_LOAD_ITEMS = false;
    }
}
