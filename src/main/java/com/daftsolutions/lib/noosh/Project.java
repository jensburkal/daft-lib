/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.daftsolutions.lib.noosh;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Represents a project in the Noosh system
 * @author Colin Manning
 */
public class Project {
    private String identity = "";
    private String name = "";
    private String description = "";
    private Date startDate = null;
    private ArrayList<SoapItem> customValues = new ArrayList<SoapItem>();

    public Project() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public ArrayList<SoapItem> getCustomValues() {
        return customValues;
    }

    public void setCustomValues(ArrayList<SoapItem> customValues) {
        this.customValues = customValues;
    }

    public void addCustomValue(SoapItem item) {
        this.customValues.add(item);
    }
}
