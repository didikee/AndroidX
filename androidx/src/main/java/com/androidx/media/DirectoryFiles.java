package com.androidx.media;

import android.os.Environment;

/**
 * user author: didikee
 * create time: 4/27/21 3:03 PM
 * description: 
 */
public enum DirectoryFiles implements StandardDirectory {
    DOWNLOADS(Environment.DIRECTORY_DOWNLOADS);

    private final String directory;

    DirectoryFiles(String directory) {
        this.directory = directory;
    }


    @Override
    public String getDirectoryName() {
        return directory;
    }
}
