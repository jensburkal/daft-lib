package com.daftsolutions.lib.utils;

import com.canto.cumulus.Asset;
import com.canto.cumulus.CumulusSession;
import com.canto.cumulus.FieldTypes;
import com.canto.cumulus.GUID;
import com.canto.cumulus.InputDataStream;
import com.canto.cumulus.Pixmap;
import com.canto.cumulus.RecordItem;
import com.canto.cumulus.Server;
import com.canto.cumulus.constants.Feature;
import com.daftsolutions.lib.ws.dam.DamFieldDescriptor;
import com.daftsolutions.lib.ws.dam.DamFieldValue;
import com.daftsolutions.lib.ws.dam.DamLabelValue;
import com.daftsolutions.lib.ws.dam.DamStringListValue;
import com.daftsolutions.lib.ws.dam.DamTableValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author colinmanning
 */
public class CumulusUtilities {

    public static byte[] getBytesFromAsset(Asset asset) throws IOException {
        byte[] result = null;
        InputDataStream is = asset.openInputDataStream();
        try {
            result = Utilities.getBytesFromInputStream(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return result;
    }

    /**
     * Sets values from a string value, e.g. from a html form input field or JSON
     *
     * @param fieldDescriptor
     * @param value
     * @return
     */
    public static DamFieldValue createCumulusFieldValue(DamFieldDescriptor fieldDescriptor, String value) throws Exception {
        DamFieldValue result = new DamFieldValue();
        switch (fieldDescriptor.dataType) {
            case FieldTypes.FieldTypeString:
                result.stringValue = value;
                result.dataType = fieldDescriptor.dataType;
                break;
            case FieldTypes.FieldTypeInteger:
                result.integerValue = new Integer(value);
                result.dataType = fieldDescriptor.dataType;
                break;
            case FieldTypes.FieldTypeLong:
                result.longValue = new Long(value);
                result.dataType = fieldDescriptor.dataType;
                break;
            case FieldTypes.FieldTypeDouble:
                result.doubleValue = new Double(value);
                result.dataType = fieldDescriptor.dataType;
                break;
            case FieldTypes.FieldTypeBool:
                result.booleanValue = Utilities.isTrue(value);
                result.dataType = fieldDescriptor.dataType;
                break;
            case FieldTypes.FieldTypeEnum:
                switch (fieldDescriptor.valueInterpretation) {
                    case FieldTypes.VALUE_INTERPRETATION_DEFAULT:
                        result.stringListValue = new DamStringListValue[1];
                        result.stringListValue[0] = new DamStringListValue();
                        result.stringListValue[0].id = new Integer(value);
                    case FieldTypes.VALUE_INTERPRETATION_STRING_ENUM_MULTIPLE_VALUES:
                        break;
                    case FieldTypes.VALUE_INTERPRETATION_STRING_ENUM_LABEL:
                        break;
                    default:
                        break;
                }
                break;
            default:
        }
        return result;
    }

        /**
     *
     * @param label
     * @return
     * @throws Exception
     */
    public static Object buildJsonLabel(DamLabelValue label) throws Exception {
        HashMap v = new HashMap();
        v.put("Id", label.id);
        v.put("DisplayString", label.displayString);
        v.put("Color", label.color);
        return new JSONObject(v);
    }

    public static Object buildJsonStringListValue(DamStringListValue[] stringList) throws Exception {
        Object result = null;
        HashMap values[] = new HashMap[stringList.length];
        if (stringList.length == 1) {
            HashMap v = new HashMap();
            v.put("Id", stringList[0].id);
            v.put("DisplayString", stringList[0].displayString);
            result = new JSONObject(v);
        } else {
            for (int i = 0; i < stringList.length; i++) {
                HashMap v = new HashMap();
                v.put("Id", stringList[i].id);
                v.put("DisplayString", stringList[i].displayString);
                values[i] = v;
            }
            result = new JSONArray(values);
        }
        return result;
    }

    
   /**
     *
     * We don't want to put binary data into JSON (maybe in the future, base64 encode). Also handle returning tables as JSONArrays
     * @param v
     * @param json if true handle returning certain types in a format suitable for JSON
     * @return
     */
    public static Object getCumulusFieldValue(DamFieldValue v, boolean json) {
        Object result = null;
        try {
            switch (v.dataType) {
                case FieldTypes.FieldTypeString:
                    result = v.stringValue;
                    break;
                case FieldTypes.FieldTypeInteger:
                    result = v.integerValue;
                    break;
                case FieldTypes.FieldTypeLong:
                    result = v.longValue;
                    break;
                case FieldTypes.FieldTypeDouble:
                    result = v.doubleValue;
                    break;
                case FieldTypes.FieldTypeBool:
                    result = v.booleanValue;
                    break;
                case FieldTypes.FieldTypePicture:
                    result = (json) ? Utilities.JSON_PICTURE_VALUE : v.byteArrayValue;
                    break;
                case FieldTypes.FieldTypeDate:
                    result = v.longValue;
                    break;
                case FieldTypes.FieldTypeBinary:
                    result = (json) ? Utilities.JSON_BINARY_VALUE : v.byteArrayValue;
                    break;
                case FieldTypes.FieldTypeEnum:
                    switch (v.valueInterpretation) {
                        case FieldTypes.VALUE_INTERPRETATION_DEFAULT:
                            result = (json) ? buildJsonStringListValue(v.stringListValue) : v.stringListValue;
                            break;
                        case FieldTypes.VALUE_INTERPRETATION_STRING_ENUM_MULTIPLE_VALUES:
                            result = (json) ? buildJsonStringListValue(v.stringListValue) : v.stringListValue;
                            break;
                        case FieldTypes.VALUE_INTERPRETATION_STRING_ENUM_LABEL:
                            result = (json) ? buildJsonLabel(v.labelValue) : v.labelValue;
                            break;
                        default:
                            result = (json) ? buildJsonStringListValue(v.stringListValue) : v.stringListValue;
                            break;
                    }
                    break;
                case FieldTypes.FieldTypeTable:
                    result = (json) ? buildJsonTable(v.tableValue) : v.tableValue;
                    break;
                default:
                    result = v.stringValue;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     *
     * @param table
     * @return
     * @throws Exception
     */
    public static JSONArray buildJsonTable(DamTableValue table) throws Exception {
        ArrayList<HashMap> rows = new ArrayList<HashMap>();
        for (int r = 0; r < table.rows.length; r++) {
            HashMap<String, Object> row = new HashMap<String, Object>();
            for (int c = 0; c < table.columnNames.length; c++) {
                row.put(table.columnNames[c], getCumulusFieldValue(table.rows[r].columns[c], true));
            }
            rows.add(row);
        }
        return new JSONArray(rows);
    }
    
    /**
     * Determines if the Cumulus server is an Enterprise Server or not.
     * Uses a combination of features that are Enterprise - in particular checks if MORE THAN 20 clients supported
     * Not sure why Cumulus API does not o this for us, as this imple,entation is unstable as Canto could change the rules!!!
     * @param server
     * @return
     */
    public static boolean isCumulusEnterpriseServer(Server server) {
        boolean result = false;
        try {
            CumulusSession cs = server.getCumulusSession();
            if (cs.getHasFeature(Feature.MORE_THAN_20_CLIENTS)
                    && cs.getHasFeature(Feature.LIVE_FILTERING)
                    && cs.getHasFeature(Feature.ORACLE_MIRROR)) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Gets the full preview for an asset as a JPEG
     * @param record
     * @return
     * @throws Exception
     */
    public static byte[] getPreviewData(RecordItem record) throws Exception {
        return Pixmap.createFromAsset(record.getAssetReferenceValue(GUID.UID_REC_ASSET_REFERENCE).getAsset(false), null).getAsJPEG();
    }

    public static Object getCumulusFieldValue(DamFieldValue v) {
        return getCumulusFieldValue(v, false);
    }

}
