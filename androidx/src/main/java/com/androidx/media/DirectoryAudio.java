package com.androidx.media;

import android.os.Environment;

/**
 * user author: didikee
 * create time: 4/27/21 3:03 PM
 * description: 
 */
public enum DirectoryAudio implements StandardDirectory {
    MUSIC(Environment.DIRECTORY_MUSIC),
    PODCASTS(Environment.DIRECTORY_PODCASTS),
    RINGTONES(Environment.DIRECTORY_RINGTONES),
    ALARMS(Environment.DIRECTORY_ALARMS),
    NOTIFICATIONS(Environment.DIRECTORY_NOTIFICATIONS);

    private final String directory;

    DirectoryAudio(String directory) {
        this.directory = directory;
    }


    @Override
    public String getDirectoryName() {
        return directory;
    }
}
