package com.heavyweightsoftware.zoomrecordingsconverter.zoom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heavyweightsoftware.zoomrecordingsconverter.HttpConnectionUtils.FullResponseBuilder;
import com.heavyweightsoftware.zoomrecordingsconverter.HttpConnectionUtils.ParameterStringBuilder;
import com.heavyweightsoftware.zoomrecordingsconverter.RecordingsAccount;
import com.heavyweightsoftware.zoomrecordingsconverter.VideoRecording;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class ZoomAccount extends RecordingsAccount {
    public static final String API_ROOT = "https://api.zoom.us/v2/users/me/";
    public static final String API_LIST_RECORDINGS = API_ROOT + "recordings";
    public static final String API_ZOOM_OATH_TOKEN = "https://zoom.us/oauth/token";

    private static final String KEY_ACCOUNT_ID = "accountId";
    private static final String KEY_AUTHORIZATION = "Authorization";
    private static final String KEY_CLIENT_ID = "clientId";
    private static final String KEY_CLIENT_SECRET = "clientSecret";

    private static final String PARM_ACCOUNT_ID = "account_id";
    private static final String PARM_FROM_DATE = " from";
    private static final String PARM_GRANT_TYPE = "grant_type";

    private static final String VALUE_ACCOUNT_CREDENTIALS = "account_credentials";

    private static final String AUTH_TYPE_BASIC = "Basic";
    private static final String AUTH_TYPE_BEARER = "Bearer";
    private static final String CLIENT_SEPARATOR = ":";
    private static final String PROP_NAME = "zoom.properties";
    private static final String REQUEST_GET = "GET";
    private static final String REQUEST_POST = "POST";

    ObjectMapper mapper = new ObjectMapper();
    private final Properties zoomProperties = new Properties();

    private String accessToken;

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
        HttpURLConnection con = buildConnection(API_ZOOM_OATH_TOKEN, REQUEST_POST);
        con.setDoOutput(true);
        setBasicAuthenticateHeaders(con);
        setAuthenticateParameters(con);
        try {
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.flush();
            out.close();
        } catch (IOException ioe) {
            throw new RuntimeException("Error authenticating", ioe);
        }

        String response = readUrlString(con);
        processAuthenticationResponse(response);
    }

    @Override
    public List<VideoRecording> listVideos(Calendar since){
        HttpURLConnection con = buildConnection(API_LIST_RECORDINGS, REQUEST_GET);
        con.setDoOutput(true);
        setBearerAuthenticateHeaders(con);
        setListVideoParameters(con, since);

        String response = readUrlString(con);
        List<VideoRecording> result = processListVideosResponse(response);
        return result;
    }

    private void setListVideoParameters(HttpURLConnection con, Calendar since) {
        Map<String, String> parms = new HashMap<>();

        String fromDate = toYMD(since);
        parms.put(PARM_FROM_DATE, fromDate);

        try {
            String value = ParameterStringBuilder.getParamsString(parms);
//            DataOutputStream out = new DataOutputStream(con.getOutputStream());
//            out.writeBytes(value);
        } catch (IOException ioe) {
            throw new RuntimeException("Error setting parameters", ioe);
        }
    }

    public static String toYMD(Calendar since) {
        StringBuilder sb = new StringBuilder();
        sb.append(since.get(Calendar.YEAR));
        sb.append('-');

        int mm = since.get(Calendar.MONTH);
        mm++;
        if (mm < 10) {
            sb.append('0');
        }
        sb.append(mm);

        sb.append('-');
        sb.append(since.get(Calendar.DATE));

        return sb.toString();
    }

    private List<VideoRecording> processListVideosResponse(String response) {
        ZoomListRecordingsPacket packet;
        try {
            packet = mapper.readValue(response, ZoomListRecordingsPacket.class);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Error parsing:" + response, jpe);
        }

        List<VideoRecording> result = buildVideoList(packet);
        return result;
    }

    public static List<VideoRecording> buildVideoList(ZoomListRecordingsPacket packet) {
        List<VideoRecording> result = new ArrayList<>();



        return result;
    }

    private HttpURLConnection buildConnection(String api, String requestType) {
        HttpURLConnection result;
        try {
            URL url = new URL(api);
            result = (HttpURLConnection) url.openConnection();
            result.setRequestMethod(requestType);
        } catch (IOException pe) {
            throw new RuntimeException("Error connecting to zoom", pe);
        }

        return result;
    }

    private void processAuthenticationResponse(String response) {
        ZoomAuthenticationPacket packet;
        try {
            packet = mapper.readValue(response, ZoomAuthenticationPacket.class);
        } catch (JsonProcessingException jse) {
            throw new RuntimeException("Error parsing:" + response, jse);
        }

        accessToken = packet.access_token;
    }

    private String readUrlString(HttpURLConnection con) {
        try {
            int status = con.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                String msg = FullResponseBuilder.getFullResponse(con) + "Request Returned error";
                throw new RuntimeException(msg);
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Error retrieving response code", ioe);
        }

        StringBuilder content = new StringBuilder();

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

    private void setBasicAuthenticateHeaders(HttpURLConnection con) {
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

        con.setRequestProperty(KEY_AUTHORIZATION, sb.toString());
    }

    private void setBearerAuthenticateHeaders(HttpURLConnection con) {
        StringBuilder sb = new StringBuilder();

        sb.append(AUTH_TYPE_BEARER);
        sb.append(" ");
        sb.append(accessToken);

        con.setRequestProperty(KEY_AUTHORIZATION, sb.toString());
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

    private static class ZoomAuthenticationPacket {
        public String access_token;
        public int expires_in;
        public String token_type;
        public String scope;
    }

    private static class ZoomListRecordingsPacket {
        public Calendar from;
        public Calendar to;
        public String next_page_token;
        public int page_count;
        public int page_size;
        public int total_records;
        public ZoomRecordedMeetingPacket[] meetings;
    }

    private static class ZoomRecordedMeetingPacket {
        public int duration;
        public String account_id;
        public String host_id;
        public TimeZone timezone;
        public long id;
        public int recording_count;
        public Calendar start_time;
        public String topic;
        public int total_size;
        public ZoomMeetingType type;
        public String uuid;
        public URL share_url;
        public ZoomRecordingFile[] recording_files;
    }

    private enum ZoomMeetingType {
        INSTANT (1),
        SCHEDULED (2),
        RECURRING_UNFIXED (3), // A recurring meeting with no fixed time.
        PMI_CREATED (4),
        PERSONAL_AUDIO_CONFERENCE (7),
        RECURRING_FIXED (8), // A recurring meeting with a fixed time.
        WEBINAR (5),
        WEBINAR_RECURRING (6),
        WEBINAR_RECURRING_FIXED (9), // A recurring webinar with a fixed time.
        UPLOADED (99); // A recording uploaded via the Recordings interface on the Zoom Web Portal.

        private int value;

        ZoomMeetingType(int valueType) {
            this.value = valueType;
        }
    }

    private enum ZoomFileExtension {
        MP4,M4A,TXT,VTT,CSV,JSON,JPG
    }

    private enum ZoomFileType {
        MP4,M4A,CHAT,TRANSCRIPT,CSV,TB,CC,CHAT_MESSAGE,SUMMARY
    }

    private enum ZoomRecordingStatus {
        completed
    }

    private enum ZoomRecordingType {
        shared_screen_with_speaker_view_cc,
        shared_screen_with_speaker_view,
        shared_screen_with_gallery_view,
        active_speaker,
        gallery_view,
        shared_screen,
        audio_only,
        audio_transcript,
        chat_file,
        poll,
        host_video,
        closed_caption,
        timeline,
        thumbnail,
        audio_interpretation,
        summary,
        summary_next_steps,
        summary_smart_chapters
    }

    private static class ZoomRecordingFile {
        public String deleted_time;
        public URL download_url;
        public String file_path;
        public int file_size;
        public ZoomFileType file_type;
        public ZoomFileExtension file_extension;
        public String id;
        public String meeting_id;
        public URL play_url;
        public String recording_end;
        public String recording_start;
        public ZoomRecordingStatus status;
        public ZoomRecordingType recording_type;
    }
}
