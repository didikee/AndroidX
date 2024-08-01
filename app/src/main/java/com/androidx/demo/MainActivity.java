package com.androidx.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.androidx.LogUtils;
import com.androidx.picker.ImageLoader;
import com.androidx.picker.MediaFolder;
import com.androidx.picker.MediaItem;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                loadPhoto();
                loadFolder();
            }
        });

    }

    private void loadPhoto() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<MediaFolder> mediaFolders = new ImageLoader().get(MainActivity.this);
                MediaFolder mediaFolder = mediaFolders.get(0);
                if (mediaFolder != null && mediaFolder.items != null) {
                    for (MediaItem item : mediaFolder.items) {
                        Log.d(TAG, "Name: " + item.getDisplayName() + " Taken: " + item.getDateTaken());
                    }
                }
            }
        }).start();
    }

    private void loadFolder() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                ArrayList<MediaFolder> mediaFolders = new ImageLoader().load(MainActivity.this, ImageLoader.IMAGE);
                final long end = System.currentTimeMillis();
                LogUtils.w("loadFolder spent: " + (end - start) / 1000);
                MediaFolder mediaFolder = mediaFolders.get(0);
                if (mediaFolder != null && mediaFolder.items != null) {
                    for (MediaItem item : mediaFolder.items) {
                        Log.d(TAG, "Name: " + item.getDisplayName() + " Taken: " + item.getDateTaken());
                    }
                }
            }
        }).start();
    }
}
