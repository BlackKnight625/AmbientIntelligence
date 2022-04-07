package com.moms.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.protobuf.ByteString;
import com.moms.app.grpc.CentralSystemFrontend;
import com.moms.app.grpc.observers.SearchItemObserver;

import java.util.ArrayList;
import java.util.List;

import pt.tecnico.moms.grpc.Communication;

public class MyItemsActivity extends AppCompatActivity {
    private List<Item> items = new ArrayList<>();
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myitems);

        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(MyItemsActivity.this,R.color.white));// set status background white

        listView = (ListView) findViewById(R.id.listview);
        CentralSystemFrontend.FRONTEND.searchItem("", new SearchItemObserver(this));

        /*
        ListView listView = (ListView) findViewById(R.id.listview);
        items.add(new Item("Toothbrush"));
        items.add(new Item("Car"));
        items.add(new Item("Zebra"));
        items.add(new Item("Remote"));

        ItemAdapter itemAdapter = new ItemAdapter(this, R.layout.list_row, items);
        listView.setAdapter(itemAdapter);
        //listView.setDivider(null);
        //listView.setDividerHeight(0);
        */

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long id) {
                Item item = (Item) adapter.getItemAtPosition(position);
                Intent intent = new Intent(getApplicationContext(), ItemActivity.class);
                intent.putExtra("item", item);
                startActivity(intent);
            }
        });

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
     * @param itemsList
     *  A list containing information about all items that comply with the search parameters
     */
    public void searchedItems(List<Communication.ItemInformation> itemsList) {
        //TODO
        for (int i = 0; i < itemsList.size(); i++) {
            String idName = itemsList.get(i).getItemId().getId();
            byte[] imageBytes = itemsList.get(i).getImage().toByteArray();
            Bitmap image = BitmapFactory.decodeByteArray(imageBytes , 0, imageBytes .length);
            String name = itemsList.get(i).getName();
            boolean lock = itemsList.get(i).getLocked();
            boolean track = itemsList.get(i).getTracked();

            items.add(new Item(name, image, track, lock, idName));
        }

        ListView listView = (ListView) findViewById(R.id.listview);
        ItemAdapter itemAdapter = new ItemAdapter(this, R.layout.list_row, items);
        listView.setAdapter(itemAdapter);
    }
}
