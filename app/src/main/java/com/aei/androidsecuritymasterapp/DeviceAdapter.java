package com.aei.androidsecuritymasterapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class DeviceAdapter extends ArrayAdapter<Device> {
    private Context context;
    List<Device> devices;
    public DeviceAdapter(@NonNull Context context, int resource, @NonNull List<Device> objects) {
        super(context, resource, objects);
        this.context = context;
        devices = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.device_item,parent,false);

        Device device = devices.get(position);

        TextView email = listItem.findViewById(R.id.device_email);
        TextView uid = listItem.findViewById(R.id.device_uid);

        email.setText(device.getEmail());
        uid.setText(device.getUid());
        return listItem;
    }
}
