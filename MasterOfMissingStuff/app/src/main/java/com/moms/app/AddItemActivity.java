package com.moms.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.protobuf.ByteString;
import com.moms.app.grpc.CentralSystemFrontend;
import com.moms.app.grpc.observers.PhotoTakenObserver;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

import pt.tecnico.moms.grpc.Communication;

public class AddItemActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.additem);

        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(AddItemActivity.this,R.color.white));// set status background white

        final Button add_picture_button = findViewById(R.id.button5);
        add_picture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Code here executes on main thread after user presses button
                //startActivity(new Intent(getApplicationContext(), ItemActivity.class));
                dispatchTakePictureIntent();
            }
        });

        final Button save_button = findViewById(R.id.button7);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Code here executes on main thread after user presses button
                //startActivity(new Intent(getApplicationContext(), ItemActivity.class));
            }
        });

        final Button back_button = findViewById(R.id.button10);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Code here executes on main thread after user presses button
                finish();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            //Compressing taken image
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);

            byte[] compressedPicture = out.toByteArray();

            //Sending image to Central System
            CentralSystemFrontend.FRONTEND.photoTaken(ByteString.copyFrom(compressedPicture), Calendar.getInstance(), new PhotoTakenObserver(this));

            ImageView imageView = (ImageView)findViewById(R.id.imageView7);
            imageView.setImageBitmap(imageBitmap);
        }
    }

    /**
     *  This method is called by the PhotoTakenObserver when a PhotoResponse is received
     * @param newItemId
     *  The item id of the new item that was classified (empty string if status != OK)
     * @param status
     *  The status of the photo's response
     */
    public void photoTakenResponseReceived(String newItemId, Communication.PhotoResponse.ResponseStatus status) {
        //TODO

        switch (status) {
            case OK:
                //TODO: Show the user the identified item's category
                break;
            case NO_ITEM_FOUND:
                break;
            case ITEM_ALREADY_EXISTS:
                break;
            case MULTIPLE_ITEMS_FOUND:
                break;
        }
    }

    /**
     *  Called when an Ack is received after the app sent a confirmation that the item being added
     * was inserted into the system
     */
    public void confirmedItemInsertion() {
        //TODO
    }

}
