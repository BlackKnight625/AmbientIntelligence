package com.moms.app;

import static java.util.stream.Collectors.toList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.protobuf.ByteString;
import com.moms.app.grpc.CentralSystemFrontend;
import com.moms.app.grpc.observers.PhotoTakenObserver;
import com.moms.app.grpc.observers.SearchItemObserver;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import pt.tecnico.moms.grpc.Communication;

public class MyItemsActivity extends AppCompatActivity {
    private ListView listView;
    private boolean isKeyboardShowing = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myitems);

        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(MyItemsActivity.this,R.color.white));// set status background white

        while(MainActivity.REQUEST_LOAD_ITEMS) {
            ;
        }
        listView = (ListView) findViewById(R.id.listview);
        ItemAdapter itemAdapter = new ItemAdapter(this, R.layout.list_row, new ArrayList(MainActivity.ITEMS.values()));
        listView.setAdapter(itemAdapter);

        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName4);
        editText.addTextChangedListener(new TextWatcher() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void afterTextChanged(Editable s) {
                List<Item> filteredItems =
                        (List<Item>) (new ArrayList(MainActivity.ITEMS.values()))
                                .stream()
                                .filter(o -> {
                                    Item item = (Item) o;
                                    String searchParams = ((EditText)findViewById(R.id.editTextTextPersonName4)).getText().toString();
                                    return item.getName().regionMatches(true, 0, searchParams, 0, searchParams.length());
                                })
                                .collect(toList());
                filteredItems.forEach(i -> System.out.println(i.getName()));
                ItemAdapter itemAdapter = new ItemAdapter(MyItemsActivity.this, R.layout.list_row, filteredItems);
                runOnUiThread(() -> listView.setAdapter(itemAdapter));
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

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
                String idName = ((Item) adapter.getItemAtPosition(position)).getIdName();
                Intent intent = new Intent(getApplicationContext(), ItemActivity.class);
                intent.putExtra("item", idName);
                startActivityForResult(intent, MainActivity.REQUEST_REMOVE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.REQUEST_REMOVE && resultCode == MainActivity.RESULT_OK) {
            ItemAdapter itemAdapter = new ItemAdapter(this, R.layout.list_row, new ArrayList(MainActivity.ITEMS.values()));
            runOnUiThread(() -> listView.setAdapter(itemAdapter));
        }
    }
}
