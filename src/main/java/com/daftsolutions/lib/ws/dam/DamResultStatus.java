/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.daftsolutions.lib.ws.dam;

/**
 *
 * @author Colin
 */
public class DamResultStatus {
   /**
    * 
    */
   public final static Integer UNKNOWN = DamFieldValue.INVALID_INTEGER;
   /**
    *
    */
   public final static Integer SUCCEED = DamFieldValue.DEFAULT_INTEGER;
   /**
    *
    */
   public final static Integer FAIL = DamFieldValue.ONE;

   /**
    *
    */
   public Integer status = UNKNOWN;
   /**
    *
    */
   public String message = DamFieldValue.EMPTY_STRING;

   /**
    *
    */
   public DamResultStatus() {
   }

}
