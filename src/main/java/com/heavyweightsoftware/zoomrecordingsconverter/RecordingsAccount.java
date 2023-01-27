package com.heavyweightsoftware.zoomrecordingsconverter;

import com.heavyweightsoftware.zoomrecordingsconverter.zoom.ZoomAccount;

import java.io.File;

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
}
