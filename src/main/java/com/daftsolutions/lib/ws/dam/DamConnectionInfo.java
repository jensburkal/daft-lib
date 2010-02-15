/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.daftsolutions.lib.ws.dam;

import java.util.ArrayList;

/**
 *
 * Simple class to hold catalog name and log indetaisl for accessing a specific catalog
 * @author Colin Manning
 */
public class DamConnectionInfo {
   public String name = DamFieldValue.EMPTY_STRING;
   public String host = DamFieldValue.EMPTY_STRING;
   public String catalogName = DamFieldValue.EMPTY_STRING;
   public String username = DamFieldValue.EMPTY_STRING;
   public String password = DamFieldValue.EMPTY_STRING;
   public ArrayList<String> viewNames = new ArrayList<String>();
   public boolean secure = false;
   public boolean readOnly = false;
   public int id = -1;
   public int licenseCount = 0;
   public int cloneCount = 1;
   public boolean cloak = false;

   public DamConnectionInfo() {
   }

   public String toString() {
       return "Connection: "+host+":"+catalogName+":"+username;
   }
}
