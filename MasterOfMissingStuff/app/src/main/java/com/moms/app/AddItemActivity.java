package com.moms.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.protobuf.ByteString;
import com.moms.app.grpc.CentralSystemFrontend;
import com.moms.app.grpc.observers.PhotoTakenObserver;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import pt.tecnico.moms.grpc.Communication;

public class AddItemActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private boolean photoTaken = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.additem);

        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(AddItemActivity.this,R.color.white));// set status background white

        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (photoTaken) {
                    enableSaveButtonIfTitleExists();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });


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
        save_button.setEnabled(false);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Code here executes on main thread after user presses button
                ImageView imageView = (ImageView) findViewById(R.id.imageView7);
                Bitmap image = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] imageBytes = stream.toByteArray();
                CentralSystemFrontend.FRONTEND.photoTaken(ByteString.copyFrom(imageBytes), Calendar.getInstance(), new PhotoTakenObserver(AddItemActivity.this));
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
            ImageView imageView = (ImageView)findViewById(R.id.imageView7);
            imageView.setImageBitmap(imageBitmap);
            photoTaken = true;
            enableSaveButtonIfTitleExists();
        }

        else if (requestCode == MainActivity.REQUEST_NEW_ITEM && resultCode == MainActivity.RESULT_OK) {

            EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
            editText.setText("");
            ((ImageView) findViewById(R.id.imageView7)).setImageDrawable(getResources().getDrawable(R.drawable.placeholder));
        }
    }

    public void enableSaveButtonIfTitleExists() {
        if (((EditText)findViewById(R.id.editTextTextPersonName)).getText().toString().trim().length() > 0) {
            findViewById(R.id.button7).setEnabled(true);
        } else {
            findViewById(R.id.button7).setEnabled(false);
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
                String name = ((EditText)findViewById(R.id.editTextTextPersonName)).getText().toString();
                ImageView imageView = (ImageView) findViewById(R.id.imageView7);
                Bitmap image = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                Item newItem = new Item(name, image, true, false, newItemId);

                MainActivity.ITEMS.put(newItemId, newItem);

                Intent intent = new Intent(getApplicationContext(), ItemActivity.class);
                intent.putExtra("item", newItemId);
                startActivityForResult(intent, MainActivity.REQUEST_NEW_ITEM);
                break;
            case NO_ITEM_FOUND:
                MainActivity.showPopupWindow(this, "No item was found in the provided picture. Try a different angle.");
                break;
            case ITEM_ALREADY_EXISTS:
                MainActivity.showPopupWindow(this, "Item of type (" + newItemId + ") already exists. You may modify/remove it in the " +
                        "'Search Item' menu.");
                break;
            case MULTIPLE_ITEMS_FOUND:
                MainActivity.showPopupWindow(this, "Multiple items were found in the picture. Make sure there are no other items near the " +
                        "item you're trying to take a picture of. For better results, place the desired item on a uniformly colored surface.");
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
