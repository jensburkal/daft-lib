package com.daftsolutions.lib.ws.dam;

public class DamFieldDescriptor {

   /**
    * the name to be used for member variables to reference this name (handle speces in field names etc.
    */
   public String memberName = "";
   /**
    *
    */
   public String name = "";
   /**
    *
    */
   public String guid = DamFieldValue.EMPTY_STRING;
   /**
    *
    */
   public Integer dataType = DamFieldValue.INVALID_INTEGER;
   /**
    *
    */
   public Integer valueInterpretation = DamFieldValue.INVALID_INTEGER;

   /**
    *
    */
   public DamFieldDescriptor() {
   }
}
