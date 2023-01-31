package com.heavyweightsoftware.zoomrecordingsconverter;

import com.heavyweightsoftware.zoomrecordingsconverter.zoom.ZoomAccount;

import java.io.File;
import java.util.Calendar;
import java.util.List;

public abstract class RecordingsAccount {
    public static final String ZOOM_ACCOUNT = "zoom";

    public static RecordingsAccount buildAccount(String downloadAccountType, File credentialsRoot) {
        String accountType = downloadAccountType.toLowerCase();
        RecordingsAccount result = switch (accountType) {
            case ZOOM_ACCOUNT -> new ZoomAccount(credentialsRoot);

            default ->  null;
        };

        if (result == null) {
            throw new RuntimeException("Bad download account type:" + downloadAccountType);
        }

        return result;
    }

    protected abstract void authenticate();

    /**
     * List the videos since the cutoff date
     * @param since the cutoff date to download videos after
     * @return a list of videos
     */
    public abstract List<VideoRecording> listVideos(Calendar since);
}
