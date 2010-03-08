/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daftsolutions.lib.ws.dam;

/**
 *
 * @author Colin
 */
public class DamAsset {

    public DamResultStatus status = new DamResultStatus();
    /**
     * The asset name
     */
    public String name = DamFieldValue.EMPTY_STRING;
    /**
     * The actual asset data
     */
    public byte[] data = new byte[0];

    /**
     *
     */
    public DamAsset() {
    }
}
