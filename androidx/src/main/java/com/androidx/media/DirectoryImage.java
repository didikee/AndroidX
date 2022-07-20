package com.androidx.media;

import android.os.Environment;

/**
 * user author: didikee
 * create time: 4/27/21 3:03 PM
 * description: 
 */
public enum DirectoryImage implements StandardDirectory{
    DCIM(Environment.DIRECTORY_DCIM),
    PICTURE(Environment.DIRECTORY_PICTURES);

    private final String directory;

    DirectoryImage(String directory) {
        this.directory = directory;
    }


    @Override
    public String getDirectoryName() {
        return directory;
    }
}
