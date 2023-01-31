package com.heavyweightsoftware.zoomrecordingsconverter.zoom

import com.fasterxml.jackson.databind.ObjectMapper
import com.heavyweightsoftware.zoomrecordingsconverter.VideoRecording
import spock.lang.Specification

class ZoomAccountTest extends Specification {
    static final String     RECORDINGS_PACKET_TEST_FILE = "testdata/ZoomVideoList.json"

    static ObjectMapper     mapper
    static ZoomAccount.ZoomListRecordingsPacket zoomListRecordingsPacket

    void setup() {
    }

    def "BuildVideoList"() {
        given: "Test data"
        ZoomAccount.ZoomListRecordingsPacket packet = getZoomListRecordingsPacket()

        when: "Converting"
        List<VideoRecording> recordingList = ZoomAccount.buildVideoList(packet)

        then: "Should be correct"
        recordingList.size() == 2
    }

    static ZoomAccount.ZoomListRecordingsPacket getZoomListRecordingsPacket() {
        if (zoomListRecordingsPacket == null) {
//            URL url = getClass().getResource(RECORDINGS_PACKET_TEST_FILE)
            File file = new File (RECORDINGS_PACKET_TEST_FILE)
            zoomListRecordingsPacket = getObjectMapper().readValue(file, ZoomAccount.ZoomListRecordingsPacket.class)
        }

        return zoomListRecordingsPacket
    }

    static ObjectMapper getObjectMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper()
        }

        return mapper
    }
}
