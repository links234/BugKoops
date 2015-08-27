package com.intel.bugkoops;

import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.StringWriter;

public class XMLRPCBuilder {
    final String LOG_TAG = getClass().getSimpleName();

    private XmlSerializer mSerializer;
    private StringWriter mWriter;

    public XMLRPCBuilder() {
        mSerializer = Xml.newSerializer();
        mWriter = new StringWriter();
    }

    public void start(String methodName) {
        mSerializer = Xml.newSerializer();
        mWriter = new StringWriter();

        try {
            mSerializer.setOutput(mWriter);

            mSerializer.startDocument("UTF-8", true);

            mSerializer.startTag("", "methodCall");
            mSerializer.startTag("", "methodName");
            mSerializer.text(methodName);
            mSerializer.endTag("", "methodName");

            mSerializer.startTag("", "params");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void end() {
        try {
            mSerializer.endTag("", "params");

            mSerializer.endTag("", "methodCall");

            mSerializer.endDocument();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void startStruct() {
        try {
            mSerializer.startTag("", "param");
            mSerializer.startTag("", "struct");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void endStruct() {
        try {
            mSerializer.endTag("", "struct");
            mSerializer.endTag("", "param");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void member(String name, String value) {
        try {
            mSerializer.startTag("", "member");
            mSerializer.startTag("", "name");
            mSerializer.text(name);
            mSerializer.endTag("", "name");
            mSerializer.startTag("", "value");
            mSerializer.startTag("", "string");
            mSerializer.text(value);
            mSerializer.endTag("", "string");
            mSerializer.endTag("", "value");
            mSerializer.endTag("", "member");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void startArray(String name) {
        try {
            mSerializer.startTag("", "member");
            mSerializer.startTag("", "name");
            mSerializer.text(name);
            mSerializer.endTag("", "name");
            mSerializer.startTag("", "value");
            mSerializer.startTag("", "array");
            mSerializer.startTag("", "data");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void endArray() {
        try {
            mSerializer.endTag("", "data");
            mSerializer.endTag("", "array");
            mSerializer.endTag("", "value");
            mSerializer.endTag("", "member");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void putValue(int value) {
        try {
            mSerializer.startTag("", "value");
            mSerializer.startTag("", "int");
            mSerializer.text(Integer.toString(value));
            mSerializer.endTag("", "int");
            mSerializer.endTag("", "value");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        return mWriter.toString();
    }
}
