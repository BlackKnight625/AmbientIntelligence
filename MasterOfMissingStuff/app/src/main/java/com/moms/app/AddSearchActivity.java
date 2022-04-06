package com.moms.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class AddSearchActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addsearch);

        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(AddSearchActivity.this,R.color.white));// set status background white

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
}
