package com.intel.bugkoops.Data;

import android.os.SystemClock;
import android.util.Log;

import java.util.HashMap;

public class MessageManager {
    public static final byte PACKET_STATUS_DONE = 0;
    public static final byte PACKET_STATUS_NOTFOUND = 1;

    private static final String LOG_TAG = MessageManager.class.getSimpleName();

    private static HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>> db = new HashMap<>();
    private static HashMap<Integer, HashMap<Integer, Long>> startTime = new HashMap<>();

    private static float lastElapsedTime = 0.0f;

    private static byte[] lastPacketStatus;

    public static boolean lastScanGotHere;

    public static void push(int messageId, int packetCount, int packetId, String result) {
        lastScanGotHere = true;

        if(db.get(messageId) == null) {
            db.put(messageId, new HashMap<Integer, HashMap<Integer, String>>());
        }
        if(db.get(messageId).get(packetCount) == null) {
            db.get(messageId).put(packetCount, new HashMap<Integer, String>());
        }

        if(startTime.get(messageId) == null) {
            startTime.put(messageId, new HashMap<Integer, Long>());
        }
        if(startTime.get(messageId).get(packetCount) == null) {
            startTime.get(messageId).put(packetCount, SystemClock.elapsedRealtime());
        }

        HashMap<Integer,String> messageDb = db.get(messageId).get(packetCount);

        messageDb.put(packetId,result);

        lastPacketStatus = new byte[packetCount];
        for(int id = 1; id <= packetCount; ++id) {
            if(messageDb.containsKey(id)) {
                lastPacketStatus[id-1] = PACKET_STATUS_DONE;
            } else {
                lastPacketStatus[id-1] = PACKET_STATUS_NOTFOUND;
            }
        }

        if(messageDb.size() == packetCount) {

            String text = "";

            for(int id = 1; id <= packetCount; ++id) {
                text += messageDb.get(id);
            }

            lastElapsedTime = SystemClock.elapsedRealtime()-startTime.get(messageId).get(packetCount);

            ReportManager.push(text);

            db.get(messageId).remove(packetCount);
            if(db.get(messageId).size()==0) {
                db.remove(messageId);
            }

            startTime.get(messageId).remove(packetCount);
            if(startTime.get(messageId).size()==0) {
                startTime.remove(messageId);
            }
        }
    }

    public static float getLastElapsedTime() {
        return lastElapsedTime;
    }

    public static byte[] getLastPacketStatus() {
        return lastPacketStatus;
    }
}
