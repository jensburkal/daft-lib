/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daftsolutions.lib.ws.dam;

/**
 *
 * @author Colin
 */
public class DamCategory {
    /**
     * Maps to the Category Name field, but many cases will only involve setting this field,
     * and it is required for searching, so make it explicit in the class.
     */
    public String name;

   /**
    * Category field values - not used for now
    */
   public DamFieldValue[] fieldValues = null;
   
   /**
    * A status value indicating if the returned category was processed successfully
    */
   public DamResultStatus status = new DamResultStatus();

   /**
    * Cumulus unique id for this category in the catalog
    */
   public Integer id = DamFieldValue.INVALID_INTEGER;

   /**
    *
    */
   public Object[] fields = null;

   /**
    * Cumulus categories are tree structured, so any category can have sub categories.
    * There is no limit to the depth the tree can be.
    */
   public DamCategory[] subCategories = new DamCategory[0];

   /**
    * Represents a Cumulus Category in a form suitable for web service exchange
    */
   public DamCategory() {
   }
}
