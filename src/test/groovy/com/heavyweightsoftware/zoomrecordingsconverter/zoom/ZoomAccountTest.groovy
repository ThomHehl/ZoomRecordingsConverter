package com.heavyweightsoftware.zoomrecordingsconverter.zoom

import com.fasterxml.jackson.databind.ObjectMapper
import com.heavyweightsoftware.zoomrecordingsconverter.VideoRecording
import spock.lang.Specification

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalField

class ZoomAccountTest extends Specification {
    static final String     RECORDINGS_PACKET_TEST_FILE = "testdata/ZoomVideoList.json"

    static final ZoneId     UTC = ZoneId.of("UTC")
    static final ZonedDateTime RECORDING1_DATE_TIME = ZonedDateTime.of(2023, 01, 30, 12, 54, 26, 0, UTC)
    static final String     RECORDING1_DOWNLOAD_URL = "https://us02web.zoom.us/rec/download/Mi3JNHwywdkkfKhG0XXnb7vz6CWxSprg-Ui9XsnPGzDW26pM4MhHn0ghNYzwixXE_Ee0b-FpSqjcuFR4.unKKWnaX6_PaXE8i"
    static final String     RECORDING1_EXTENSION = ".mp4"
    static final String     RECORDING1_TITLE = "Radically Fit"
    static final String     RECORDING1_UUID = "oVvkZtomQDWn4T6OUI52AA=="

    static final ZonedDateTime RECORDING2_DATE_TIME = ZonedDateTime.of(2023, 01, 29, 21, 30, 9, 0, UTC)
    static final String     RECORDING2_DOWNLOAD_URL = "https://us02web.zoom.us/rec/download/ihh-1g_h8eTIi7oPXg9_yzk3loT0BQ8Z3opBci1-aRyMk0KThZeKY03pUZtFBb6K-dSDBom84_NskLBN.CpPPBNNvErJ25drj"
    static final String     RECORDING2_EXTENSION = ".mp4"
    static final String     RECORDING2_TITLE = "Fascia Release & Flow"
    static final String     RECORDING2_UUID = "pkC8XohtTuuL7fPd38FGEg=="

    static ObjectMapper     mapper
    static ZoomAccount.ZoomListRecordingsPacket zoomListRecordingsPacket

    void setup() {
    }

    def "BuildVideoList"() {
        given: "Test data"
        ZoomAccount.ZoomListRecordingsPacket packet = getZoomListRecordingsPacket()

        when: "Converting"
        List<VideoRecording> recordingList = ZoomAccount.buildVideoList(packet)
        VideoRecording vr1 = recordingList.get(0)
        VideoRecording vr2 = recordingList.get(1)

        then: "Should be correct"
        recordingList.size() == 2

        vr1.getUuid() == RECORDING1_UUID
        vr1.getTitle() == RECORDING1_TITLE
        vr1.getDownloadUrl().toString() == RECORDING1_DOWNLOAD_URL
        vr1.getExtension() == RECORDING1_EXTENSION
        vr1.getDateTime() == RECORDING1_DATE_TIME

        vr2.getUuid() == RECORDING2_UUID
        vr2.getTitle() == RECORDING2_TITLE
        vr2.getDownloadUrl().toString() == RECORDING2_DOWNLOAD_URL
        vr2.getExtension() == RECORDING2_EXTENSION
        vr2.getDateTime() == RECORDING2_DATE_TIME
    }

    static ZoomAccount.ZoomListRecordingsPacket getZoomListRecordingsPacket() {
        if (zoomListRecordingsPacket == null) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(RECORDINGS_PACKET_TEST_FILE)
            File file = new File (url.getFile())
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
