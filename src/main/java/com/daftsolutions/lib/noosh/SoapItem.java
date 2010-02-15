/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.daftsolutions.lib.noosh;

/**
 *
 * @author colin
 */
public class SoapItem {

    public final static String TYPE_STRING = "string";
    public final static String TYPE_LONG = "long";
    public final static String TYPE_DATETIME = "dateTime";

    private String key = null;
    private String value = null;
    private String type = TYPE_STRING;

    public SoapItem() {

    }

    public SoapItem(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public SoapItem(String key, String value, String type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
