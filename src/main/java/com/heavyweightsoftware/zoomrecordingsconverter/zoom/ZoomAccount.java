package com.heavyweightsoftware.zoomrecordingsconverter.zoom;

import com.heavyweightsoftware.zoomrecordingsconverter.HttpConnectionUtils.FullResponseBuilder;
import com.heavyweightsoftware.zoomrecordingsconverter.HttpConnectionUtils.ParameterStringBuilder;
import com.heavyweightsoftware.zoomrecordingsconverter.RecordingsAccount;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ZoomAccount extends RecordingsAccount {
    private static final String PARM_ACCOUNT_ID = "account_id";
    private static final String PARM_GRANT_TYPE = "grant_type";

    private static final String KEY_ACCOUNT_ID = "accountId";
    private static final String KEY_AUTHORIZATION = "Authorization";
    private static final String KEY_CLIENT_ID = "clientId";
    private static final String KEY_CLIENT_SECRET = "clientSecret";

    private static final String VALUE_ACCOUNT_CREDENTIALS = "account_credentials";

    private static final String AUTH_TYPE_BASIC = "Basic";
    private static final String CLIENT_SEPARATOR = ":";
    private static final String PROP_NAME = "zoom.properties";
    private static final String REQUEST_POST = "POST";

    private Properties zoomProperties = new Properties();

    public ZoomAccount(File credentialsRoot) {
        loadZoomProperties(credentialsRoot);
        authenticate();
    }

    private void loadZoomProperties(File credentialsRoot) {
        File propFile = new File(credentialsRoot, PROP_NAME);
        try {
            FileReader fileReader = new FileReader(propFile);
            BufferedReader reader = new BufferedReader(fileReader);
            zoomProperties.load(reader);
        } catch (IOException ioe) {
            throw new RuntimeException("Error loading properties:" + PROP_NAME, ioe);
        }
    }

    @Override
    protected void authenticate() {
        HttpURLConnection con;
        try {
            URL url = new URL("https://zoom.us/oauth/token");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(REQUEST_POST);
        } catch (ProtocolException pe) {
            throw new RuntimeException("Error connecting to zoom", pe);
        } catch (IOException ioe) {
            throw new RuntimeException("Error connecting to zoom", ioe);
        }

        con.setDoOutput(true);
        setAuthenticateHeaders(con);
        setAuthenticateParameters(con);
        try {
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.flush();
            out.close();
        } catch (IOException ioe) {
            throw new RuntimeException("Error authenticating", ioe);
        }

        try {
            int status = con.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                String msg = FullResponseBuilder.getFullResponse(con) + "Authentication Returned error";
                throw new RuntimeException(msg);
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Error retrieving response code", ioe);
        }

        String authCode = readUrlString(con);
    }

    private void handleConnectionError(String msg, HttpURLConnection con, int status) {
        throw new RuntimeException("Authenticate returned " + status);
    }

    private String readUrlString(HttpURLConnection con) {
        StringBuffer content = new StringBuffer();

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
        } catch (IOException ioe) {
            throw new RuntimeException("Error reading web content", ioe);
        }

        con.disconnect();

        return content.toString();
    }

    private void setAuthenticateHeaders(HttpURLConnection con) {
        StringBuilder sb = new StringBuilder();
        sb.append(zoomProperties.getProperty(KEY_CLIENT_ID));
        sb.append(CLIENT_SEPARATOR);
        sb.append(zoomProperties.getProperty(KEY_CLIENT_SECRET));
        String clientString = sb.toString();

        Base64.Encoder encoder = Base64.getEncoder();
        String encodedString = encoder.encodeToString(clientString.getBytes());

        sb.setLength(0);
        sb.append(AUTH_TYPE_BASIC);
        sb.append(" ");
        sb.append(encodedString);

        con.setRequestProperty(KEY_AUTHORIZATION, encodedString);
    }

    private void setAuthenticateParameters(HttpURLConnection con) {
        Map<String, String> parms = new HashMap<>();

        String acctId = zoomProperties.getProperty(KEY_ACCOUNT_ID);
        parms.put(PARM_ACCOUNT_ID, acctId);
        parms.put(PARM_GRANT_TYPE, VALUE_ACCOUNT_CREDENTIALS);

        try {
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(ParameterStringBuilder.getParamsString(parms));
        } catch (IOException ioe) {
            throw new RuntimeException("Error setting parameters", ioe);
        }
    }
}
