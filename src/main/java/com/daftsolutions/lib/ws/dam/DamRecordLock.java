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
    private long lockedTime = 0;

    private boolean success = false;

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

    public long getLockedTime() {
        return lockedTime;
    }

    public void setLockedTime(long lockedTime) {
        this.lockedTime = lockedTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public DamRecordLock() {
    }
}
