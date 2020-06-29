package com.aei.androidsecuritymasterapp.storage;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class StorageUtils {
    private StorageReference mStorageRef;
    public interface StorageUtilsDownloadCallback{
        void onFileReady(File file);
    }
    public interface StorageUtilsUploadCallback{
        void onFinish(String url);
    }
    public StorageUtils(String url){
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
    }
    public void downloadFile(File to, StorageUtilsDownloadCallback callback){
        mStorageRef.getFile(to)
                .addOnSuccessListener(taskSnapshot -> callback.onFileReady(to))
                .addOnFailureListener(e -> callback.onFileReady(null));
    }
    public void uploadFile(File file, String path, StorageUtilsUploadCallback callback){
        StorageReference ref = mStorageRef
                .child(path + "/" + file.getName());
        Uri uri = Uri.fromFile(file);
        ref.putFile(uri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                .addOnSuccessListener(result->callback.onFinish(result.toString())));
    }
}
