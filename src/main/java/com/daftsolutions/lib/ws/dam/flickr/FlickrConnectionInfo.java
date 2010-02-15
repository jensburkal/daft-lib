/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.daftsolutions.lib.ws.dam;

/**
 *
 * @author colin
 */
public class FlickrConnectionInfo {
   public String name = DamFieldValue.EMPTY_STRING;
   public String host = DamFieldValue.EMPTY_STRING;
   public String catalogName = DamFieldValue.EMPTY_STRING;
   public String username = DamFieldValue.EMPTY_STRING;
   public String password = DamFieldValue.EMPTY_STRING;
   public boolean secure = false;
   public boolean readOnly = false;
   public boolean cloak = false;
   public int cloakSeed = 0;

   public FlickrConnectionInfo() {
   }

   public String toString() {
       return "Flickr connection: "+host+":"+catalogName+":"+username;
   }

}
