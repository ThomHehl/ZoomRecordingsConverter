package com.heavyweightsoftware.zoomrecordingsconverter;

import java.io.*;
import java.net.URL;
import java.util.Properties;

public class Application {
    private static final String CONTROL_FILE = "control.properties";
    private static final String CREDENTIALS_PATH = "/credentials/";
    private static final String DOWNLOAD = "download";

    private Properties controlProperties = new Properties();
    private File credentialsRoot;
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
        String downloadAccountType = String.valueOf(controlProperties.get(DOWNLOAD));
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
}
