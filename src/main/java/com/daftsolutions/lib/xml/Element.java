/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daftsolutions.lib.xml;

import java.util.ArrayList;
import org.json.JSONObject;

/**
 *
 * @author Colin Manning
 */
public class Element {

    // class variables
    private Element parent = null;
    private String name = null;
    private String content = null;
    private ArrayList<Attribute> attributes = new ArrayList<Attribute>();
    private ArrayList<Element> elements = new ArrayList<Element>();

    public Element(String name) throws Exception {
        this(null, name, null);
    }

    public Element(Element parent, String name) throws Exception {
        this(parent, name, null);
    }

    public Element(Element parent, String name, String content) throws Exception {
        this.parent = parent;
        this.name = name;
        this.content = content;
        if (parent != null) {
            parent.addElement(this);
        }
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public String getName() {
        return name;
    }

    public Element getParent() {
        return parent;
    }

    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }

    public void addElement(Element element) throws Exception {
        if (content != null) {
            throw new Exception("Cannot add element to element with existing content");
        }
        elements.add(element);
    }

    public String toPrettyString() {
        return toString();
    }

    @Override
    public String toString() {
        String result = "<" + name;
        for (Attribute attribute : attributes) {
            result += " " + attribute;
        }
        if (content != null) {
            result += ">" + content+ "</" + name + ">";
        } else if (elements.size() > 0) {
            result += ">";
            for (Element element : elements) {
                result += "\n" + element;
            }
            result += "\n" + "</" + name + ">";
        } else {
            result += "/>";
        }
        return result;
    }

    public JSONObject toJson() {
        JSONObject result = null;
        return result;
    }
}
