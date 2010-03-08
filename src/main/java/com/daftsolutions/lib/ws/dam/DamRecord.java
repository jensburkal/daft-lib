/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daftsolutions.lib.ws.dam;

import java.util.Date;

public class DamRecord {

    /**
     *
     */
    public DamResultStatus status = new DamResultStatus();
    /**
     *
     */
    public Integer catalogId;
    /**
     *
     */
    public Integer id;
    /**
     *
     */
    public DamFieldValue[] fieldValues = null;
    /**
     *
     */
    public DamCategory[] keywords = null;
    /**
     * The asset version number, -1 if not versioned
     */
    public int version = -1;
    /**
     * If this asset is a variant, this is the parent (assumes one to many for now)
     */
    public int parentId = -1;
    /**
     * List of any variants of this asset, just the ids
     */
    public int[] variants = new int[0];

    /**
     *
     */
    public DamRecord() {
    }
}
