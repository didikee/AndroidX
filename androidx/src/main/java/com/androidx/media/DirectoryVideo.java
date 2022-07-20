package com.androidx.media;

import android.os.Environment;

/**
 * user author: didikee
 * create time: 4/27/21 3:03 PM
 * description: 
 */
public enum DirectoryVideo implements StandardDirectory {
    MOVIE(Environment.DIRECTORY_MOVIES),
    DCIM(Environment.DIRECTORY_DCIM);

    private final String directory;

    DirectoryVideo(String directory) {
        this.directory = directory;
    }


    @Override
    public String getDirectoryName() {
        return directory;
    }
}
