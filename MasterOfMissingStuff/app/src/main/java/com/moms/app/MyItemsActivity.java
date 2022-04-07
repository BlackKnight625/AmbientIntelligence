package com.moms.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import pt.tecnico.moms.grpc.Communication;

public class MyItemsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myitems);

        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(MyItemsActivity.this,R.color.white));// set status background white

        ListView listView = (ListView) findViewById(R.id.listview);
        List<Item> items = new ArrayList<>();
        items.add(new Item("Toothbrush"));
        items.add(new Item("Car"));
        items.add(new Item("Zebra"));
        items.add(new Item("Remote"));

        ItemAdapter itemAdapter = new ItemAdapter(this, R.layout.list_row, items);
        listView.setAdapter(itemAdapter);

        final Button back_button = findViewById(R.id.button9);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Code here executes on main thread after user presses button
                finish();
            }
        });
    }

    /**
     *  Called when information about items is received after a search request is sent
     * @param itemInformations
     *  A list containing information about all items that comply with the search parameters
     */
    public void searchedItems(List<Communication.ItemInformation> itemInformations) {
        //TODO
    }
}
