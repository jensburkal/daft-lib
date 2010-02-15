/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.daftsolutions.lib.utils;

import com.canto.cumulus.GUID;

/**
 * Make a Cumulus Guid, that is guaranted to be
 * GUID is simply written to standard output
 * @author colin
 */
public class MakeGuid {
    public static void main(String[] args) {
        System.out.println(new GUID().toString());
    }

}
