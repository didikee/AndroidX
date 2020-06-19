package com.androidx.demo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.androidx.picker.ImageLoader;
import com.androidx.picker.MediaFolder;
import com.androidx.picker.MediaItem;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPhoto();
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
                        Log.d(TAG, "Name: " + item.getDisplayName() +" Taken: "+ item.getDateTaken());
                    }
                }
            }
        }).start();
    }
}
