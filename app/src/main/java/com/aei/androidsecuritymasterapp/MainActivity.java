package com.aei.androidsecuritymasterapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Device> devices;
    private DeviceAdapter deviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = this;
        devices  = new ArrayList<>();
        ListView devicesListView = findViewById(R.id.devices_list_view);
        deviceAdapter = new DeviceAdapter(context,
                R.layout.device_item,devices);
        devicesListView.setAdapter(deviceAdapter);

        devicesListView.setOnItemClickListener((parent, view, position, id)->{
            Intent intent = new Intent(this, DeviceActivity.class);
            intent.putExtra("uid",devices.get(position).getUid());
            startActivity(intent);
        });
        getPermissions();
    }

    private ValueEventListener devicesListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            devices.clear();
            Log.e("DB","Got " + dataSnapshot.getKey());
            for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Log.e("Firebase","" + snapshot.getKey());
                String email = snapshot.child("email").getValue(String.class);
                devices.add(new Device().setUid(snapshot.getKey()).setEmail(email));
            }
            deviceAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public void getPermissions(){
        // Check whether this app has write external storage permission or not.
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        ArrayList<String> permissions = new ArrayList<>();
        if(writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissions.size() > 0)
            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[0]),23);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseDatabase.getInstance().getReference()
                .child("devices")
                .addValueEventListener(devicesListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseDatabase.getInstance().getReference()
                .child("devices").removeEventListener(devicesListener);
    }
}
