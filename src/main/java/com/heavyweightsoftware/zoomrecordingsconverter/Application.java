package com.heavyweightsoftware.zoomrecordingsconverter;

import java.io.*;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

public class Application {
    private static final String CONTROL_FILE = "control.properties";
    private static final String CREDENTIALS_PATH = "/credentials/";
    private static final String DOWNLOAD = "download";
    private static final String NUMBER_OF_DAYS = "numberOfDays";

    private Properties controlProperties = new Properties();
    private File credentialsRoot;
    private int numberOfDays;
    private File resources;

    private RecordingsAccount downloadAccount;

    public Application() {
        buildPaths();
        buildAccounts();
    }

    protected void buildAccounts() {
        loadControlProperties();
        createAccounts();
    }

    protected void buildPaths() {
        URL url = getClass().getResource(CREDENTIALS_PATH);
        if (url == null) {
            throw new RuntimeException("Unable to find credentials directory.");
        }

        credentialsRoot = new File(url.getPath());
        if (credentialsRoot == null) {
            throw new RuntimeException("Unable to find credentials directory.");
        }

        resources = credentialsRoot.getParentFile();
    }

    protected void createAccounts() {
        String downloadAccountType = controlProperties.getProperty(DOWNLOAD);
        downloadAccount = RecordingsAccount.buildAccount(downloadAccountType, credentialsRoot);
    }

    protected void loadControlProperties() {
        File controlFile = new File(resources, CONTROL_FILE);
        try {
            FileReader fileReader = new FileReader(controlFile);
            Reader reader = new BufferedReader(fileReader);
            controlProperties.load(reader);
        } catch (IOException ioe) {
            throw new RuntimeException("Error loading " + CONTROL_FILE, ioe);
        }
    }

    public void downloadVideos() {
        Calendar cutoffDate = getCutoffDate();
        List<VideoRecording> videos = downloadAccount.listVideos(cutoffDate);
    }

    private Calendar getCutoffDate() {
        String numDays = controlProperties.getProperty(NUMBER_OF_DAYS, "14");
        numberOfDays = Integer.parseInt(numDays);
        Calendar result = new GregorianCalendar();
        result.add(Calendar.DATE, -numberOfDays);
        result.add(Calendar.DATE, -1);
        return result;
    }
}
