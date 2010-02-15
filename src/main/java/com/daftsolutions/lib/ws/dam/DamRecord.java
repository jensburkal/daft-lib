/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daftsolutions.lib.ws.dam;

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
    *
    */
   public DamRecord() {

   }
}
