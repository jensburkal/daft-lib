/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daftsolutions.lib.ws.dam;

import java.util.Date;

/**
 *
 * @author colin
 */
public class DamRecordLock {

    /**
     * The record id
     */
    private int id = -1;
    /**
     * Indicates if the asset is locked
     */
    private boolean locked = false;
    /**
     * Name of user who has locked the asset, if any
     */
    private String lockedBy = "";
    /**
     * Time asset was locked
     */
    private Date lockedTime = null;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    public Date getLockedTime() {
        return lockedTime;
    }

    public void setLockedTime(Date lockedTime) {
        this.lockedTime = lockedTime;
    }

    public DamRecordLock() {
    }
}
