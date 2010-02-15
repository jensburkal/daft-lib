/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daftsolutions.lib.ws.dam;

/**
 *
 * @author Colin
 */
public class DamFieldValue {

    public final static Integer INVALID_INTEGER = -1;
    public final static Integer DEFAULT_INTEGER = 0;
    public final static Long DEFAULT_LONG = 0L;
    public final static Double DEFAULT_DOUBLE = 0.0D;
    public final static String EMPTY_STRING = "";
    public final static Integer ONE = 1;
    public final static Boolean TRUE = true;
    public final static Boolean FALSE = false;
    /**
     *
     */
    public Integer dataType = INVALID_INTEGER;
    /**
     *
     */
    public Integer valueInterpretation = INVALID_INTEGER;
    /**
     *
     */
    public String stringValue = EMPTY_STRING;
    /**
     *
     */
    public Integer integerValue = new Integer(0);
    /**
     *
     */
    public Long longValue = DEFAULT_LONG;
    /**
     *
     */
    public Double doubleValue = DEFAULT_DOUBLE;
    /**
     *
     */
    public Boolean booleanValue = FALSE;
    /**
     *
     */
    public byte[] byteArrayValue = new byte[0];
    /**
     *
     */
    public DamStringListValue[] stringListValue = new DamStringListValue[0];
    /**
     *
     */
    public DamLabelValue labelValue = new DamLabelValue();
    /**
     *
     */
    public DamTableValue tableValue = new DamTableValue();

    /**
     *
     */
    public DamFieldValue() {
    }


}
