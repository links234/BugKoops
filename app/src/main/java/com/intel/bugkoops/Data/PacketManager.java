package com.intel.bugkoops.Data;

import android.util.Log;

import com.intel.bugkoops.Utility;

import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PacketManager {
    private static final String LOG_TAG = PacketManager.class.getSimpleName();

    private static final byte BK1_MAGIC_FIRSTBYTE = (byte) 222;
    private static final byte BK1_MAGIC_SECONDBYTE = (byte) 173;

    private static final byte BK1_ENCODE_NONE = (byte) 0;
    private static final byte BK1_ENCODE_DEFLATE = (byte) 1;

    public static void push(byte[] data) {
        if (data.length < 2 || data[0] != BK1_MAGIC_FIRSTBYTE || data[1] != BK1_MAGIC_SECONDBYTE) {
            ReportManager.push(Utility.bytesToString(data));
        } else {
            data = Arrays.copyOfRange(data, 2, data.length);
            addBK1Packet(data);
        }
    }

    private static void addBK1Packet(byte[] data) {
        if (data.length < 4) {
            Log.e(LOG_TAG, "BK1: packet header is not present");
            return;
        }

        byte version = data[0];
        byte messageId = data[1];
        byte packetCount = data[2];
        byte packetId = data[3];
        byte encode = data[4];
        byte checkSum = data[5];

        if(Utility.xor(data, 0, 6) != 0) {
            Log.e(LOG_TAG, "BK1: Invalid header (checksum failed)");
            return;
        }

        if(version > 0) {
            Log.e(LOG_TAG, "BK1: Unsupported version!");
            return;
        }

        if (packetId > packetCount) {
            Log.e(LOG_TAG, "BK1: Invalid packetId");
            return;
        }

        data = Arrays.copyOfRange(data, 6, data.length);

        DecodeResult result = new DecodeResult();
        switch (encode) {
            case BK1_ENCODE_NONE:
                result = BK1DecodeNone(data);
                break;
            case BK1_ENCODE_DEFLATE:
                result = BK1DecodeDeflate(data);
                break;
            default:
                Log.e(LOG_TAG, "BK1: Invalid encode byte!");
                result.message = "";
                result.status = DecodeResult.STATUS_FAILED;
                break;
        }

        if(result.status == DecodeResult.STATUS_FAILED) {
            Log.e(LOG_TAG, "BK1: Failed to decode packet!");
        } else {
            MessageManager.push(messageId, packetCount, packetId, result.message);
        }
    }

    private static DecodeResult BK1DecodeNone(byte[] data) {
        DecodeResult result = new DecodeResult();

        result.message = Utility.bytesToString(data);

        Log.d(LOG_TAG, result.message);

        return result;
    }

    private static DecodeResult BK1DecodeDeflate(byte[] data) {
        DecodeResult result = new DecodeResult();

        if (data.length < 2) {
            Log.e(LOG_TAG, "BK1: deflate header is not present");
            result.message = "";
            result.status = DecodeResult.STATUS_FAILED;
            return result;
        }

        int decompressedByteCount = Utility.bytesToInt(data[0], data[1]);
        data = Arrays.copyOfRange(data, 2, data.length);

        boolean failed = false;

        Inflater inflater = new Inflater();
        inflater.setInput(data, 0, data.length);
        byte[] decompressedBytes = new byte[decompressedByteCount];
        try {
            if (inflater.inflate(decompressedBytes) != decompressedByteCount) {
                Log.e(LOG_TAG, "BK1 Deflate: Header data size does not match decompressed data size!");
                failed = true;
            }
        } catch (DataFormatException e) {
            Log.e(LOG_TAG, "BK1 Deflate: Invalid data format! ");
            failed = true;
        }
        inflater.end();

        if (failed) {
            Log.e(LOG_TAG, "BK1 Deflate: Failed to decode deflate data!");
            result.status = DecodeResult.STATUS_FAILED;
        } else {
            result.message = Utility.bytesToString(decompressedBytes);
        }

        return result;
    }

    public static class DecodeResult {
        public static final int STATUS_OK = 0;
        public static final int STATUS_FAILED = 1;

        public DecodeResult() {
            status = STATUS_OK;
            message = "";
        }

        public int status;
        public String message;
    }
}
