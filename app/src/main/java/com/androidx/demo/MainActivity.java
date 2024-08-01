package com.androidx.demo;

import android.os.Bundle;
import android.view.View;

import com.androidx.LogUtils;
import com.androidx.picker.ImageLoader;
import com.androidx.picker.MediaFolder;
import com.androidx.picker.MediaItem;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnQueryDataSourceListener;

import java.util.ArrayList;
import java.util.List;

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
                loadPhoto();
//                loadFolder();
//                testPhotoSelectorSDK();
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
//                    for (MediaItem item : mediaFolder.items) {
//                        Log.d(TAG, "Name: " + item.getDisplayName() + " Taken: " + item.getDateTaken());
//                    }
                    LogUtils.d("Items count: " + mediaFolder.items.size());
                }
            }
        }).start();
    }

    private void testPhotoSelectorSDK() {
        final long start = System.currentTimeMillis();
        PictureSelector.create(this)
                .dataSource(SelectMimeType.ofImage())
                .obtainMediaData(new OnQueryDataSourceListener<LocalMedia>() {
                    @Override
                    public void onComplete(List<LocalMedia> result) {
                        LogUtils.w("loadFolder spent: " + (System.currentTimeMillis() - start));
                        LogUtils.d("PictureSelector onComplete: " + result.size());
                    }
                });
    }
}
