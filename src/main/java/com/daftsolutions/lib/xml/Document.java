/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daftsolutions.lib.xml;

import org.json.JSONObject;

/**
 * A simple XML Document, no validation or verification, just genrates what is requested
 * @author Colin Manning
 */
public class Document {

    // class variables
    private String encoding = null;
    private Element root = null;


    public Document(String encoding, Element root) {
        this.encoding = encoding;
        this.root = root;
    }

    public String toPrettyString() {
        return toString();
    }

    @Override
    public String toString() {
        String result = "<?xml version=\"1.0\" encoding=\""+encoding+"\"?>";
        result += "\n"+root;
        return result;
    }

    public JSONObject toJson() {
        JSONObject result = null;
        return result;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public Element getRoot() {
        return root;
    }
}
