package com.heavyweightsoftware.zoomrecordingsconverter.HttpConnectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.List;

public class FullResponseBuilder {
    public static String getFullResponse(HttpURLConnection con) throws IOException {
        StringBuilder fullResponseBuilder = new StringBuilder();

        fullResponseBuilder.append(con.getResponseCode())
                .append(" ")
                .append(con.getResponseMessage())
                .append("\n");

        con.getHeaderFields().entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .forEach(entry -> {
                    fullResponseBuilder.append(entry.getKey()).append(": ");
                    List headerValues = entry.getValue();
                    Iterator it = headerValues.iterator();
                    if (it.hasNext()) {
                        fullResponseBuilder.append(it.next());
                        while (it.hasNext()) {
                            fullResponseBuilder.append(", ").append(it.next());
                        }
                    }
                    fullResponseBuilder.append("\n");
                });

        InputStreamReader streamReader = new InputStreamReader(con.getErrorStream());
        BufferedReader reader = new BufferedReader(streamReader);

        try {
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                fullResponseBuilder.append(inputLine);
            }
            reader.close();
        } catch (IOException ioe) {
            throw new RuntimeException("Error reading web content", ioe);
        }
        fullResponseBuilder.append('\n');

        return fullResponseBuilder.toString();
    }
}
