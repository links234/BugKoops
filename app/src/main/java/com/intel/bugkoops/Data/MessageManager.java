package com.intel.bugkoops.Data;

import android.os.SystemClock;
import android.util.Log;

import java.util.HashMap;

public class MessageManager {
    private static final String LOG_TAG = MessageManager.class.getSimpleName();

    private static HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>> db = new HashMap<>();
    private static HashMap<Integer, HashMap<Integer, Long>> startTime = new HashMap<>();

    private static float lastElapsedTime = 0.0f;

    public static void push(int messageId, int packetCount, int packetId, String result) {
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

        Log.d(LOG_TAG, "messageId = "+Integer.toString(messageId));
        Log.d(LOG_TAG, "packetCount = "+Integer.toString(packetCount));
        Log.d(LOG_TAG, "packetId = " + Integer.toString(packetId));

        HashMap<Integer,String> messageDb = db.get(messageId).get(packetCount);

        messageDb.put(packetId,result);

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

}
