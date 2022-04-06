package com.moms.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.rpc.context.AttributeContext;

import java.util.List;

public class ItemAdapter extends ArrayAdapter<Item> {
    private Context _context;
    private int _resource;


    public ItemAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        _context = context;
        _resource = resource;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(_context);
        convertView = layoutInflater.inflate(_resource, parent, false);
        ImageView imageView = convertView.findViewById(R.id.imageView10);
        TextView txtView = convertView.findViewById(R.id.textView7);

        //imageView.setImageBitmap(getItem(position).getImage());
        txtView.setText(getItem(position).getName());

        return convertView;
    }
}
