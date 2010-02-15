package com.daftsolutions.lib.ws.beans;

import com.canto.cumulus.AllCategoriesItemCollection;
import com.canto.cumulus.Asset;
import com.canto.cumulus.Catalog;
import com.canto.cumulus.CategoryItem;
import com.canto.cumulus.CategoryItemCollection;
import com.canto.cumulus.Cumulus;
import com.canto.cumulus.CumulusException;
import com.canto.cumulus.FieldDefinition;
import com.canto.cumulus.FieldTypes;
import com.canto.cumulus.GUID;
import com.canto.cumulus.InputDataStream;
import com.canto.cumulus.Item;
import com.canto.cumulus.ItemCollection;
import com.canto.cumulus.Layout;
import com.canto.cumulus.MultiRecordItemCollection;
import com.canto.cumulus.Pixmap;
import com.canto.cumulus.RecordItem;
import com.canto.cumulus.RecordItemCollection;
import com.canto.cumulus.Server;
import com.canto.cumulus.constants.CatalogingFlag;
import com.canto.cumulus.events.CatalogingEventObject;
import com.canto.cumulus.events.CatalogingListener;
import com.canto.cumulus.exceptions.LoginFailedException;
import com.canto.cumulus.exceptions.QueryParserException;
import com.canto.cumulus.fieldvalue.AssetReference;
import com.canto.cumulus.fieldvalue.CategoriesFieldValue;
import com.canto.cumulus.fieldvalue.LabelFieldValue;
import com.canto.cumulus.fieldvalue.StringEnumFieldValue;
import com.canto.cumulus.usermanagement.AuthenticationManager;
import com.canto.cumulus.usermanagement.FieldValues;
import com.canto.cumulus.usermanagement.User;
import com.canto.cumulus.usermanagement.UserFieldDefinition;
import com.canto.cumulus.utils.ImagingPixmap;
import com.canto.cumulus.utils.LanguageManager;
import com.canto.cumulus.utils.PlatformUtils;
import com.daftsolutions.lib.pool.CumulusConnectionPool;
import com.daftsolutions.lib.pool.RecordResultSet;
import com.daftsolutions.lib.utils.CumulusUtilities;
import com.daftsolutions.lib.utils.Utilities;
import com.daftsolutions.lib.ws.dam.DamAsset;
import com.daftsolutions.lib.ws.dam.DamCategory;
import com.daftsolutions.lib.ws.dam.DamConnectionInfo;
import com.daftsolutions.lib.ws.dam.DamRecordCollection;
import com.daftsolutions.lib.ws.dam.DamFieldDescriptor;
import com.daftsolutions.lib.ws.dam.DamFieldValue;
import com.daftsolutions.lib.ws.dam.DamLabelValue;
import com.daftsolutions.lib.ws.dam.DamPreview;
import com.daftsolutions.lib.ws.dam.DamRecord;
import com.daftsolutions.lib.ws.dam.DamResultStatus;
import com.daftsolutions.lib.ws.dam.DamStringListValue;
import com.daftsolutions.lib.ws.dam.DamTableItemValue;
import com.daftsolutions.lib.ws.dam.DamTableValue;
import com.daftsolutions.lib.ws.dam.DamUser;
import com.daftsolutions.lib.ws.dam.DamUserComment;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;

/**
 *
 * @author Colin
 */
public class CumulusBean extends DamBean {

    private static Logger logger = Logger.getLogger(CumulusBean.class);
    public final static String NAME = "cumulus";
    // constants
    public final static String CUMULUS_DEFAULT_USERNAME = "cumulus-default-username";
    public final static String CUMULUS_DEFAULT_PASSWORD = "cumulus-default-password";
    public final static String CUMULUS_ASSET_HANDLING_SET = "cumulus-asset-handling-set";
    public final static String CUMULUS_ASSET_ACTION = "cumulus-asset-action";
    // various stuff
    private Server cumulusServer = null;
    private Map<String, Catalog> catalogs = null;
    private Map<String, CumulusConnectionPool> connectionPools = null; // key is "catalogName:username" - e.g. "ImageBank:colin"
    private HashMap<String, DamUser> users = null;

    /**
     *
     */
    public CumulusBean() {
        super();
        connectionPools = new HashMap<String, CumulusConnectionPool>();
    }

    public void terminate() {
        try {
            for (CumulusConnectionPool connectionPool : connectionPools.values()) {
                connectionPool.terminate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void init(Properties properties) {
        try {
            this.properties = properties;
            users = new HashMap<String, DamUser>();
            logger.info("CumulusWebServiceBean init called");
            Cumulus.CumulusStart();

            logger.info("   --- cache-dir is: " + (String) properties.getProperty(CACHE_DIR));
            cacheDir = new File((String) properties.getProperty(CACHE_DIR));
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            logger.info("   --- temp-dir is: " + (String) properties.getProperty(TEMP_DIR));
            tempDir = new File((String) properties.getProperty(TEMP_DIR));
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            Cumulus.CumulusStart();
            catalogs = new HashMap<String, Catalog>();
            logger.info("CumulusWebServiceBean init done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Catalog getCatalog(String catalogName) throws Exception {
        Catalog result = catalogs.get(catalogName);
        if (result == null) {
            result = cumulusServer.openCatalog(cumulusServer.findCatalogID(catalogName));
            catalogs.put(catalogName, result);
        }
        return result;
    }

    /**
     *
     * @param connection catalog name username and password for connection to Cumulus
     * @param fieldDescriptor
     * @param recordId
     * @param fieldGuid
     * @param locale
     * @param username
     * @param password
     * @return
     */
    public DamFieldValue getRecordField(DamConnectionInfo connection, Integer recordId, String fieldGuid, String locale) {
        DamFieldValue result = new DamFieldValue();
        RecordItem recordItem = null;
        try {
            Locale loc = null;
            if (locale != null) {
                loc = getLocale(locale);
            } else {
                loc = Locale.getDefault();
            }
            recordItem = getPool(connection).getRecordItemById(recordId, false);
            result = getFieldValue(recordItem, new GUID(fieldGuid), getPool(connection).getRecordLayout(), loc);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            getPool(connection).releaseReadRecordItem(recordItem);
        }
        return result;
    }

    /**
     *
     * @param connection catalog name username and password for connection to Cumulus
     * @param fieldDescriptor
     * @param record
     * @param locale
     * @param username
     * @param password
     * @return
     */
    public DamResultStatus setRecordField(DamConnectionInfo connection, Integer recordId, String fieldGuid, DamFieldValue fieldValue, String locale) {
        DamResultStatus result = new DamResultStatus();
        RecordItem recordItem = null;
        try {
            Locale loc = null;
            if (locale != null) {
                loc = getLocale(locale);
            } else {
                loc = Locale.getDefault();
            }
            recordItem = getPool(connection).getRecordItemById(recordId, true);
            setFieldValue(recordItem, new GUID(fieldGuid), fieldValue, getPool(connection).getRecordLayout(), loc);
            recordItem.save();
            result.status = DamResultStatus.SUCCEED;
        } catch (Exception e) {
            result.status = DamResultStatus.FAIL;
            e.printStackTrace();
        } finally {
            getPool(connection).releaseWriteRecordItem(recordItem);
        }
        return result;
    }

    /**
     *
     * @param connection
     * @param fieldDescriptors
     * @param record
     * @param categories
     * @param locale
     * @return
     */
    public DamResultStatus updateRecord(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, DamRecord record, DamCategory[] categories, String locale) {
        return updateRecords(connection, fieldDescriptors, new DamRecord[]{record}, categories, locale)[0];
    }

    /**
     *
     * @param connection
     * @param fieldDescriptors
     * @param records
     * @param categories
     * @param locale
     * @return
     */
    public DamResultStatus[] updateRecords(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, DamRecord[] records, DamCategory[] categories, String locale) {
        DamResultStatus[] result = new DamResultStatus[records.length];
        RecordItem recordItem = null;
        CategoryItem rootCategory = null;
        try {
            for (int i = 0; i < records.length; i++) {
                try {
                    result[i] = new DamResultStatus();
                    recordItem = getPool(connection).getRecordItemById(records[i].id, true);
                    DamFieldValue[] fieldValues = records[i].fieldValues;
                    boolean doSave = false;
                    if (fieldDescriptors != null && fieldValues != null && fieldDescriptors.length == fieldValues.length) {
                        // update fields
                        Locale loc = null;
                        if (locale != null) {
                            loc = getLocale(locale);
                        } else {
                            loc = Locale.getDefault();
                        }
                        for (int f = 0; f < fieldDescriptors.length; f++) {
                            if (fieldValues[f].dataType != DamFieldValue.INVALID_INTEGER) {
                                setFieldValue(recordItem, new GUID(fieldDescriptors[f].guid), fieldValues[f], getPool(connection).getRecordLayout(), loc);
                            }
                        }
                        doSave = true;
                    }

                    // now process the categories
                    if (categories != null && categories.length > 0) {
                        AllCategoriesItemCollection allCategoriesItemCollection = getPool(connection).getAllCategoriesItemCollection(true);
                        rootCategory = allCategoriesItemCollection.getCategoryTreeCatalogRootCategory();
                        // first remove existing categories
                        // assumes a full update - existing ones deleted
                        // TODO parameterise this to make it optional
                        CategoriesFieldValue cfv = recordItem.getCategoriesValue();
                        if (cfv.hasValue()) {
                            cfv.removeIDs(cfv.getIDs());
                            cfv.clearValue();
                            recordItem.setCategoriesValue(cfv);
                            recordItem.save();
                        }
                        // refetch, as category removal does not seem to persist in the recordItem, although in the database
                        // TODO report this as a bug to Canto and remove once fixed
                        getPool(connection).releaseWriteRecordItem(recordItem);
                        recordItem = getPool(connection).getRecordItemById(records[i].id, true);
                        cfv = recordItem.getCategoriesValue();
                        CategoryItem parentCategory = rootCategory;
                        processCategories(recordItem, parentCategory, allCategoriesItemCollection, categories);
                        doSave = true;
                    }
                    if (doSave) {
                        recordItem.save();
                        result[i].status = DamResultStatus.SUCCEED;
                    }
                } catch (Exception re) {
                    result[i].status = DamResultStatus.FAIL;
                    re.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            getPool(connection).releaseWriteRecordItem(recordItem);
        }
        return result;
    }

    /**
     * Return the id of a single record. If query returns more than one, only the id of the first is returned
     * @param connection
     * @param query
     * @param locale
     * @return
     */
    public Integer findRecord(DamConnectionInfo connection, String query, String locale) {
        Integer result = -1;
        try {
            try {
                result = getPool(connection).findRecord(query, locale);
            } catch (QueryParserException qpe) {
                logger.info("problem parsing Cumulus query: '" + query + "'");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public DamRecordCollection findRecordsByQuickSearch(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, String quickSearch, Integer offset, Integer count, String locale) {
        DamRecordCollection result = new DamRecordCollection();
        try {
            String query = null;
            logger.debug("calling findRecordsByQuickSearch with terms '" + quickSearch + " for connection '" + connection.catalogName + "'");
            MultiRecordItemCollection c = null;
            RecordItemCollection rc = null;
            CumulusConnectionPool thePool = getPool(connection);
            try {
                c = thePool.getMasterServer().newMultiRecordItemCollection();
                // make sure we clone for now, as multi catalog collection seems to close the added collections
                //TODO sort this out so that normal pool borrowing can work
                //TODO refactor convertQuickSearchToComplexQuery to work with simple collections if possible
                rc = (RecordItemCollection) thePool.cloneObjectToRead(RecordItemCollection.class);
                c.addItemCollection(rc);
                query = PlatformUtils.convertQuickSearchToComplexQuery(quickSearch, c);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
            logger.debug("   --- resulting query is '" + query + "'");
            if (query != null) {
                return findRecords(connection, fieldDescriptors, query, offset, count, locale);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *
     * @param connection catalog name username and password for connection to Cumulus
     * @param fieldDescriptors
     * @param query
     * @param offset
     * @param count
     * @param locale
     * @param username
     * @param password
     * @return
     */
    public DamRecordCollection findRecords(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, String query, Integer offset, Integer count, String locale) {
        DamRecordCollection result = new DamRecordCollection();
        try {
            logger.debug("calling findRecords with query '" + query + "' for connection '" + connection.catalogName + "'");
            Layout layout = getPool(connection).getRecordLayout();
            RecordResultSet results = null;
            boolean ok = false;
            try {
                results = getPool(connection).findRecords(query, offset, count, locale);
                ok = true;
            } catch (QueryParserException qpe) {
                logger.info("problem parsing Cumulus query: '" + query + "'");
            }
            if (ok) {
                GUID[] guids = new GUID[fieldDescriptors.length];
                for (int f = 0; f < fieldDescriptors.length; f++) {
                    try {
                        if (fieldDescriptors[f].guid != null && !"".equals(fieldDescriptors[f].guid)) {
                            guids[f] = new GUID(fieldDescriptors[f].guid);
                        }
                    } catch (Exception e) {
                        // do nothing
                    }
                }
                // sort out paging
                result.offset = results.offset;
                result.totalCount = results.totalCount;
                if (results.offset < result.totalCount) { // if offset out of range, return empty list
                    result.pageCount = results.data.length;
                    result.records = new DamRecord[results.data.length];
                    int i = 0;
                    for (Integer recordId : results.data) {
                        result.records[i] = new DamRecord();
                        RecordItem recordItem = getPool(connection).getRecordItemById(recordId, false);
                        result.records[i].id = recordItem.getID();
                        result.records[i].fieldValues = new DamFieldValue[fieldDescriptors.length];
                        for (int f = 0; f < fieldDescriptors.length; f++) {
                            if (guids[f] == null) {
                                // return an invalid field type (client to check for -1)
                                result.records[i].fieldValues[f] = new DamFieldValue();
                                continue;
                            }
                            DamFieldDescriptor fieldDescriptor = fieldDescriptors[f];
                            result.records[i].fieldValues[f] = getFieldValue(recordItem, guids[f], layout, getLocale(locale));
                            result.records[i].fieldValues[f].dataType = fieldDescriptor.dataType;
                            result.records[i].fieldValues[f].valueInterpretation = fieldDescriptor.valueInterpretation;
                        }
                        // look for keywords
                        CategoriesFieldValue categories = recordItem.getCategoriesValue();
                        if (categories != null) {
                            Set<String> keywordSet = new HashSet<String>(); // use a set to avoid duplicates
                            AllCategoriesItemCollection allCategoriesItemCollection = getPool(connection).getAllCategoriesItemCollection();
                            for (Integer categoryId : categories.getIDs()) {
                                CategoryItem categoryItem = allCategoriesItemCollection.getCategoryItemByID(categoryId);
                                //logger.info("Category tree path: " + categoryItem.getCategoryTreePath());
                                String categoryTreePath = categoryItem.getCategoryTreePath();
                                if (categoryTreePath.startsWith("$Keywords")) {
                                    String[] bits = categoryTreePath.split(":");
                                    for (int j = 1; j < bits.length; j++) { // ignore the forst one of course
                                        keywordSet.add(bits[j]);
                                    }
                                }
                            }
                            ArrayList<DamCategory> cumulusCategories = new ArrayList<DamCategory>();
                            for (String keyword : keywordSet) {
                                DamCategory cumulusCategory = new DamCategory();
                                cumulusCategory.name = keyword;
                                cumulusCategories.add(cumulusCategory);
                            }
                            cumulusCategories.trimToSize();
                            result.records[i].keywords = cumulusCategories.toArray(new DamCategory[0]);
                        }
                        i++;
                        getPool(connection).releaseReadRecordItem(recordItem);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *
     * @param connection catalog name username and password for connection to Cumulus
     * @param fieldDescriptors
     * @param recordId
     * @param locale
     * @param username
     * @param password
     * @return
     */
    public DamRecord getRecordById(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, Integer recordId, String locale) {
        DamRecord result = new DamRecord();
        RecordItem recordItem = null;
        try {
            Layout layout = getPool(connection).getRecordLayout();
            recordItem = getPool(connection).getRecordItemById(recordId, false);
            GUID[] guids = new GUID[fieldDescriptors.length];
            for (int f = 0; f < fieldDescriptors.length; f++) {
                try {
                    if (fieldDescriptors[f].guid != null && !"".equals(fieldDescriptors[f].guid)) {
                        guids[f] = new GUID(fieldDescriptors[f].guid);
                    }
                } catch (Exception e) {
                    // do nothing
                }
            }
            result = new DamRecord();
            result.id = recordItem.getID();
            result.fieldValues = new DamFieldValue[fieldDescriptors.length];
            for (int f = 0; f < fieldDescriptors.length; f++) {
                if (guids[f] == null) {
                    // return an invalid field type (client to check for -1)
                    result.fieldValues[f] = new DamFieldValue();
                    continue;
                }
                DamFieldDescriptor fieldDescriptor = fieldDescriptors[f];
                result.fieldValues[f] = getFieldValue(recordItem, guids[f], layout, getLocale(locale));
                result.fieldValues[f].dataType = fieldDescriptor.dataType;
                result.fieldValues[f].valueInterpretation = fieldDescriptor.valueInterpretation;
            }
            // look for keywords
            CategoriesFieldValue categories = recordItem.getCategoriesValue();
            if (categories != null) {
                Set<String> keywordSet = new HashSet<String>(); // use a set to avoid duplicates
                AllCategoriesItemCollection allCategoriesItemCollection = getPool(connection).getAllCategoriesItemCollection();
                for (Integer categoryId : categories.getIDs()) {
                    CategoryItem categoryItem = allCategoriesItemCollection.getCategoryItemByID(categoryId);
                    //logger.info("Category tree path: " + categoryItem.getCategoryTreePath());
                    String categoryTreePath = categoryItem.getCategoryTreePath();
                    if (categoryTreePath.startsWith("$Keywords")) {
                        String[] bits = categoryTreePath.split(":");
                        for (int j = 1; j < bits.length; j++) { // ignore the forst one of course
                            keywordSet.add(bits[j]);
                        }
                    }
                }
                ArrayList<DamCategory> cumulusCategories = new ArrayList<DamCategory>();
                for (String keyword : keywordSet) {
                    DamCategory cumulusCategory = new DamCategory();
                    cumulusCategory.name = keyword;
                    cumulusCategories.add(cumulusCategory);
                }
                cumulusCategories.trimToSize();
                result.keywords = cumulusCategories.toArray(new DamCategory[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            getPool(connection).releaseReadRecordItem(recordItem);
        }
        return result;
    }

    /**
     *
     * @param connection catalog name username and password for connection to Cumulus
     * @param fieldDescriptors
     * @param categoryId
     * @param locale
     * @param username
     * @param password
     * @return
     */
    public DamCategory getCategoryById(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, Integer categoryId, String locale) {
        DamCategory result = new DamCategory();
        CategoryItem categoryItem = null;
        try {
            Layout layout = getPool(connection).getCategoryLayout();
            categoryItem = getPool(connection).getCategoryItemById(categoryId, false);
            GUID[] guids = new GUID[fieldDescriptors.length];
            for (int f = 0; f < fieldDescriptors.length; f++) {
                try {
                    if (fieldDescriptors[f].guid != null && !"".equals(fieldDescriptors[f].guid)) {
                        guids[f] = new GUID(fieldDescriptors[f].guid);
                    }
                } catch (Exception e) {
                    // do nothing
                }
            }
            result = new DamCategory();
            result.id = categoryItem.getID();
            result.fieldValues = new DamFieldValue[fieldDescriptors.length];
            for (int f = 0; f < fieldDescriptors.length; f++) {
                if (guids[f] == null) {
                    // return an invalid field type (client to check for -1)
                    result.fieldValues[f] = new DamFieldValue();
                    continue;
                }
                DamFieldDescriptor fieldDescriptor = fieldDescriptors[f];
                result.fieldValues[f] = getFieldValue(categoryItem, guids[f], layout, getLocale(locale));
                result.fieldValues[f].dataType = fieldDescriptor.dataType;
                result.fieldValues[f].valueInterpretation = fieldDescriptor.valueInterpretation;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            getPool(connection).releaseReadCategoryItem(categoryItem);
        }
        return result;
    }

    /**
     * @param connection catalog name username and password for connection to Cumulus
     * @param fieldGuids
     * @param query
     * @param locale
     * @param root
     * @param recursive
     * @return
     */
    public DamCategory findCategories(DamConnectionInfo connection, String categoryPath) {
        DamCategory result = new DamCategory();
        CumulusConnectionPool thePool = null;
        CategoryItemCollection collection = null;
        CategoryItem rootCategory = null;
        try {
            thePool = getPool(connection);
            collection = (CategoryItemCollection) thePool.borrowObjectToRead(CategoryItemCollection.class);
            if (collection != null) {
                rootCategory = collection.getCategoryItemByID(collection.getCategoryTreeItemIDByPath(categoryPath));
                result = walkCategoryTree(rootCategory);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //thePool.releaseReadCategoryItem(categoryItem);
            thePool.returnReadObject(collection);
        }
        return result;
    }

    /**
     * Returns information about a user, based on credentials in the specified connection
     * Hard coding of Cumulus user field names here for now
     * @param connection server, host, username, password and catalog to check
     * @return a CumulusUser object if the credentials can be validated and access is available for the catalog, otherwise null
     */
    public DamUser getUser(DamConnectionInfo connection, DamConnectionInfo testConnection) {
        DamUser result = null;
        if (result == null) {
            try {
                // TODO get the connection values from web.xml
                logger.debug("trying to get user details");
                Server server = Server.openConnection(false, testConnection.host, testConnection.username, testConnection.password);
                Integer catalogId = server.findCatalogID(testConnection.catalogName);
                logger.debug("--- catalogId: " + catalogId);
                Set<Integer> catalogIds = server.getCatalogIDs(false, false);
                boolean found = false;
                for (Integer testId : catalogIds) {
                    if (catalogId == testId) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    //TODO if possible, do not return guest user
                    logger.debug("--- getting auth stuff");
                    result = users.get(testConnection.toString());
                    if (result == null) {
                        AuthenticationManager am = AuthenticationManager.getAuthenticationManager(getPool(connection).getMasterServer());
                        //TODO get these setup globally for each server (maybe via the pool)
                        UserFieldDefinition[] fieldDefs = am.getFieldDefinitions();
                        User user = am.getUser(testConnection.username);
                        FieldValues fieldValues = user.getFieldValues();
                        result = new DamUser();
                        result.setUsername(testConnection.username);
                        result.setRoles(user.getRoleNames().getNames().toArray(new String[0]));
                        //TODO get Canto to publish GUIDs for $User catalog fields in GUID class as constants
                        //TODO get rid of hard coded field names
                        for (UserFieldDefinition fieldDef : fieldDefs) {
                            if (fieldDef.getName(Cumulus.getLanguageID()).equals("First Name")) {
                                result.setFirstName(fieldValues.getValue(fieldDef.getGUID(), ""));
                            } else if (fieldDef.getName(Cumulus.getLanguageID()).equals("Last Name")) {
                                result.setLastName(fieldValues.getValue(fieldDef.getGUID(), ""));
                            } else if (fieldDef.getName(Cumulus.getLanguageID()).equals("E-Mail Address")) {
                                result.setEmail(fieldValues.getValue(fieldDef.getGUID(), ""));
                            }
                        }
                        users.put(testConnection.toString(), result);
                    }
                }
            } catch (LoginFailedException lfe) {
                logger.debug("attempt to validate connection: '" + connection.toString() + " failed");
                // authentication failed, so just return null
            } catch (CumulusException ce) {
                logger.debug("attempt to validate connection: '" + connection.toString() + " failed");
                // authentication failed, so just return null - could be admin user authentication attempt, which is not allowed
                ce.printStackTrace();
            } catch (Exception e) {
                logger.debug("attempt to validate connection: '" + connection.toString() + " failed");
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     *
     * @param connection catalog name username and password for connection to Cumulus
     * @param fieldNames
     * @return
     */
    public DamFieldDescriptor[] getCatalogRecordFieldDescriptors(DamConnectionInfo connection, String[] fieldNames) {
        DamFieldDescriptor[] result = new DamFieldDescriptor[fieldNames.length];
        try {
            Layout layout = getPool(connection).getRecordLayout();
            HashMap<String, GUID> fieldNameGuids = new HashMap<String, GUID>();
            for (GUID guid : layout.getFieldUIDs()) {
                FieldDefinition fieldDefinition = layout.getFieldDefinition(guid);
                fieldNameGuids.put(fieldDefinition.getName(), guid);
            }
            for (int i = 0; i < fieldNames.length; i++) {
                result[i] = new DamFieldDescriptor();
                result[i].name = fieldNames[i];
                GUID guid = fieldNameGuids.get(fieldNames[i]);
                if (guid == null) {
                    logger.info("Record field named '" + fieldNames[i] + "' not found in Cumulus catalog:" + connection.catalogName);
                    continue;
                }
                FieldDefinition fieldDefinition = layout.getFieldDefinition(guid);
                result[i].dataType = fieldDefinition.getFieldType();
                result[i].valueInterpretation = fieldDefinition.getValueInterpretation();
                result[i].guid = (guid == null) ? null : guid.toString();
                logger.debug("Record field named '" + fieldNames[i] + "' has type: " + result[i].dataType
                        + " and valueInterpretation: " + result[i].valueInterpretation + " in catalog: " + connection.catalogName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *
     * @param connection catalog name username and password for connection to Cumulus
     * @param fieldNames
     * @return
     */
    public DamFieldDescriptor[] getCatalogAllRecordFieldDescriptors(DamConnectionInfo connection) {
        DamFieldDescriptor[] result = new DamFieldDescriptor[0];
        try {
            Layout layout = getPool(connection).getRecordLayout();
            Set<GUID> guids = layout.getFieldUIDs();
            result = new DamFieldDescriptor[guids.size()];
            int i = 0;
            for (GUID guid : guids) {
                FieldDefinition fieldDefinition = layout.getFieldDefinition(guid);
                result[i] = new DamFieldDescriptor();
                result[i].name = fieldDefinition.getName();
                result[i].dataType = fieldDefinition.getFieldType();
                result[i].valueInterpretation = fieldDefinition.getValueInterpretation();
                result[i].guid = (guid == null) ? null : guid.toString();
                logger.debug("Record field named '" + result[i].name + "' has type: " + result[i].dataType
                        + " and valueInterpretation: " + result[i].valueInterpretation + " in catalog: " + connection.catalogName);
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *
     * @param connection catalog name username and password for connection to Cumulus
     * @param fieldNames
     * @return
     */
    public DamFieldDescriptor[] getCatalogCategoryFieldDescriptors(DamConnectionInfo connection, String[] fieldNames) {
        DamFieldDescriptor[] result = new DamFieldDescriptor[fieldNames.length];
        try {
            Layout layout = getPool(connection).getCategoryLayout();
            HashMap<String, GUID> fieldNameGuids = new HashMap<String, GUID>();
            for (GUID guid : layout.getFieldUIDs()) {
                FieldDefinition fieldDefinition = layout.getFieldDefinition(guid);
                fieldNameGuids.put(fieldDefinition.getName(), guid);
            }
            for (int i = 0; i < fieldNames.length; i++) {
                result[i] = new DamFieldDescriptor();
                result[i].name = fieldNames[i];
                GUID guid = fieldNameGuids.get(fieldNames[i]);
                if (guid == null) {
                    logger.info("Category field named '" + fieldNames[i] + "' not found in Cumulus catalog:" + connection.catalogName);
                    continue;
                }
                FieldDefinition fieldDefinition = layout.getFieldDefinition(guid);
                result[i].dataType = fieldDefinition.getFieldType();
                result[i].valueInterpretation = fieldDefinition.getValueInterpretation();
                result[i].guid = (guid == null) ? null : guid.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *
     * @param connection
     * @param id
     */
    public void deleteAsset(DamConnectionInfo connection, Integer id) {
        deleteAssets(connection, new Integer[]{id});
    }

    /**
     *
     * @param connection
     * @param id
     * @param deleteAsset
     */
    public void deleteRecord(DamConnectionInfo connection, Integer id, boolean deleteAsset) {
        deleteRecords(connection, new Integer[]{id}, deleteAsset);
    }

    /**
     *
     * @param connection
     * @param ids
     * @param deleteAssets
     */
    public void deleteRecords(DamConnectionInfo connection, Integer[] ids, boolean deleteAssets) {
        getPool(connection).deleteRecordItems(ids, deleteAssets);
    }

    /**
     *
     * @param connection
     * @param ids
     */
    public void deleteAssets(DamConnectionInfo connection, Integer[] ids) {
        deleteRecords(connection, ids, true);
    }

    /**
     *
     * @param connection
     * @param id
     * @return
     */
    public void deleteCategory(DamConnectionInfo connection, Integer id) {
        deleteCategories(connection, new Integer[]{id});
    }

    /**
     *
     * @param connection
     * @param ids
     * @return
     */
    public void deleteCategories(DamConnectionInfo connection, Integer[] ids) {
        getPool(connection).deletetCategoryItems(ids);
    }

    /**
     *
     * @param connection catalog name username and password for connection to Cumulus
     * @param assets
     * @param assetHandlingSet
     * @param fieldDescriptors
     * @param fieldValues
     * @paral locale
     * @param username
     * @param password
     * @return
     */
    public DamRecord[] createAssetsWithData(DamConnectionInfo connection, DamAsset[] assets, DamCategory[] categories, String assetHandlingSet,
            DamFieldDescriptor[] fieldDescriptors, DamFieldValue[] fieldValues, String locale) {
        DamRecord[] result = new DamRecord[assets.length];
        CumulusConnectionPool thePool = null;
        RecordItemCollection recordCollection = null;
        RecordItem recordItem = null;
        CategoryItem rootCategory = null;
        File tempFile = null;
        try {
            thePool = getPool(connection);
            recordCollection = (RecordItemCollection) thePool.borrowObjectToWrite(RecordItemCollection.class);
            if (recordCollection != null) {
                Layout layout = recordCollection.getLayout();
                WsCatalogingListener catalogingListener = new WsCatalogingListener();
                recordCollection.addCatalogingListener((CatalogingListener<ItemCollection>) catalogingListener);
                for (int i = 0; i < assets.length; i++) {
                    logger.debug("processing asset: " + assets[i].name);
                    result[i] = new DamRecord();
                    if (!tempDir.exists()) {
                        tempDir.mkdirs();
                    }
                    tempFile = new File(tempDir, assets[i].name);
                    FileOutputStream os = new FileOutputStream(tempFile);
                    os.write(assets[i].data);
                    os.close();
                    Asset asset = new Asset(recordCollection.getCumulusSession(), tempFile);
                    recordCollection.catalogAsset(asset, assetHandlingSet, null, EnumSet.noneOf(CatalogingFlag.class), (CatalogingListener<ItemCollection>) catalogingListener);
                    result[i].id = catalogingListener.getItemId();
                    recordCollection.addItemByID(result[i].id); // to be sure in case asset handling set prevents this
                    boolean doSave = false;
                    recordItem = recordCollection.getRecordItemByID(result[i].id);
                    if (fieldDescriptors != null && fieldValues != null && fieldDescriptors.length == fieldValues.length) {
                        // update fields
                        Locale loc = null;
                        if (locale != null) {
                            loc = getLocale(locale);
                        } else {
                            loc = Locale.getDefault();
                        }
                        for (int f = 0; f < fieldDescriptors.length; f++) {
                            GUID guid = new GUID(fieldDescriptors[f].guid);
                            setFieldValue(recordItem, guid, fieldValues[f], layout, loc);
                        }
                        doSave = true;
                    }
                    if (categories != null && categories.length > 0) {
                        AllCategoriesItemCollection allCategoriesItemCollection = thePool.getAllCategoriesItemCollection(true);
                        rootCategory = allCategoriesItemCollection.getCategoryTreeCatalogRootCategory();
                        CategoryItem parentCategory = rootCategory;
                        processCategories(recordItem, parentCategory, allCategoriesItemCollection, categories);
                        doSave = true;
                    }
                    if (doSave) {
                        recordItem.save();
                    }
                    logger.info("catalog asset: " + tempFile.getName() + " done with record id: " + result[i].id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            thePool.returnWriteObject(recordCollection);
        }
        return result;
    }

    /**
     *
     * @param connection
     * @param path
     * @return
     */
    public DamCategory createCategoryFromPath(DamConnectionInfo connection, String path) {
        DamCategory result = new DamCategory();
        CumulusConnectionPool thePool = null;
        CategoryItemCollection categoryCollection = null;
        CategoryItem rootCategory = null;
        try {
            thePool = getPool(connection);
            categoryCollection = (CategoryItemCollection) thePool.borrowObjectToWrite(CategoryItemCollection.class);
            if (categoryCollection != null) {
                rootCategory = categoryCollection.getCategoryTreeCatalogRootCategory();
                CategoryItem categoryItem = rootCategory.createCategoryItems(path);
                result.id = categoryItem.getID();
                result.name = categoryItem.getStringValue(GUID.UID_CAT_NAME);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (thePool != null && categoryCollection != null) {
                thePool.returnWriteObject(categoryCollection);
            }
        }
        return result;
    }

    /**
     *
     * @param connection
     * @param id parent category id
     * @param name new category name
     * @return
     */
    public DamCategory createSubCategory(DamConnectionInfo connection, Integer id, String name) {
        DamCategory result = new DamCategory();
        CumulusConnectionPool thePool = null;
        CategoryItemCollection categoryCollection = null;
        CategoryItem category = null;
        try {
            thePool = getPool(connection);
            categoryCollection = (CategoryItemCollection) thePool.borrowObjectToWrite(CategoryItemCollection.class);
            if (categoryCollection != null) {
                category = categoryCollection.getCategoryItemByID(id);
                CategoryItem categoryItem = category.createCategoryItem(name);
                result.id = categoryItem.getID();
                result.name = categoryItem.getStringValue(GUID.UID_CAT_NAME);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (thePool != null && categoryCollection != null) {
                thePool.returnWriteObject(categoryCollection);
            }
        }
        return result;
    }

    public DamResultStatus addRecordToCategory(DamConnectionInfo connection, int categoryId, int recordId) {
        DamResultStatus result = new DamResultStatus();
        RecordItem recordItem = null;
        try {
            recordItem = getPool(connection).getRecordItemById(recordId, true);
            if (recordItem != null) {
                CategoriesFieldValue cfv = recordItem.getCategoriesValue();
                cfv.addID(categoryId);
                recordItem.setCategoriesValue(cfv);
                recordItem.save();
            }
            result.status = DamResultStatus.SUCCEED;
        } catch (Exception e) {
            result.status = DamResultStatus.FAIL;
            e.printStackTrace();
        } finally {
            getPool(connection).releaseReadRecordItem(recordItem);
        }
        return result;
    }

    /**
     *
     * @param connection catalog name username and password for connection to Cumulus
     * @param asset
     * @param file
     * @param assetHandlingSet
     * @param fieldDescriptors
     * @param fieldValues
     * @paral locale
     * @param username
     * @param password
     * @return
     */
    public DamRecord createAssetWithData(DamConnectionInfo connection, DamAsset asset, File file, DamCategory[] categories, String assetHandlingSet,
            DamFieldDescriptor[] fieldDescriptors, DamFieldValue[] fieldValues, String locale) {
        DamRecord result = new DamRecord();
        CumulusConnectionPool thePool = null;
        RecordItemCollection recordCollection = null;
        CategoryItem rootCategory = null;
        try {
            thePool = getPool(connection);
            recordCollection = (RecordItemCollection) thePool.borrowObjectToWrite(RecordItemCollection.class);
            if (recordCollection != null) {
                Layout layout = recordCollection.getLayout();
                WsCatalogingListener catalogingListener = new WsCatalogingListener();
                recordCollection.addCatalogingListener((CatalogingListener<ItemCollection>) catalogingListener);
                result = new DamRecord();
                Asset cumulusAsset = new Asset(recordCollection.getCumulusSession(), file);
                logger.debug(" --- about to catalog asset: " + file.getName());
                recordCollection.catalogAsset(cumulusAsset, assetHandlingSet, null, EnumSet.noneOf(CatalogingFlag.class), (CatalogingListener<ItemCollection>) catalogingListener);
                logger.debug(" --- asset cataloging done: " + file.getName());
                result.id = catalogingListener.getItemId();
                boolean doSave = false;
                RecordItem recordItem = recordCollection.getRecordItemByID(result.id);
                if (fieldDescriptors != null && fieldValues != null && fieldDescriptors.length == fieldValues.length) {
                    // update fields
                    Locale loc = null;
                    if (locale != null) {
                        loc = getLocale(locale);
                    } else {
                        loc = Locale.getDefault();
                    }
                    for (int f = 0; f < fieldDescriptors.length; f++) {
                        GUID guid = new GUID(fieldDescriptors[f].guid);
                        setFieldValue(recordItem, guid, fieldValues[f], layout, loc);
                    }
                    doSave = true;
                }
                if (categories != null && categories.length > 0) {
                    AllCategoriesItemCollection allCategoriesItemCollection = thePool.getAllCategoriesItemCollection(true);
                    rootCategory = allCategoriesItemCollection.getCategoryTreeCatalogRootCategory();
                    CategoryItem parentCategory = rootCategory;
                    processCategories(recordItem, parentCategory, allCategoriesItemCollection, categories);
                    doSave = true;
                }
                if (doSave) {
                    recordItem.save();
                }
                logger.info(" catalog asset: " + file.getName() + " done with record id: " + result.id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (thePool != null && recordCollection != null) {
                thePool.returnWriteObject(recordCollection);
            }
        }
        return result;
    }

    /**
     *
     * @param connection
     * @param assets
     * @param assetHandlingSet
     * @return
     */
    public DamRecord[] createAssets(DamConnectionInfo connection, DamAsset[] assets, DamCategory[] categories, String assetHandlingSet) {
        return createAssetsWithData(connection, assets, categories, assetHandlingSet, null, null, null);
    }

    /**
     *
     * @param connection catalog name username and password for connection to Cumulus
     * @param records
     * @param username
     * @param password
     * @return
     */
    public DamAsset[] downloadAssets(DamConnectionInfo connection, DamRecord[] records, String assetAction) {
        DamAsset[] result = new DamAsset[records.length];
        CumulusConnectionPool thePool = null;
        RecordItemCollection collection = null;
        try {
            thePool = getPool(connection);
            collection = (RecordItemCollection) thePool.borrowObjectToWrite(RecordItemCollection.class);
            if (collection != null) {
                for (int i = 0; i < records.length; i++) {
                    result[i] = new DamAsset();
                    RecordItem recordItem = collection.getRecordItemByID(records[i].id);
                    result[i].name = recordItem.getStringValue(GUID.UID_REC_ASSET_NAME);
                    AssetReference assetReference = recordItem.getAssetReferenceValue(GUID.UID_REC_ASSET_REFERENCE);
                    // result[i].data = Utilities.getBytesFromFile(assetReference.getAsset(true).getAsFile());
                    // make sure it works with Vault
                    result[i].data = CumulusUtilities.getBytesFromAsset(assetReference.getAsset(false));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (thePool != null && collection != null) {
                thePool.returnWriteObject(collection);
            }
        }
        return result;
    }

    /**
     *
     * @param connection catalog name username and password for connection to Cumulus
     * @param id
     * @return
     */
    public DamAsset downloadAsset(DamConnectionInfo connection, int id) {
        DamAsset result = new DamAsset();
        CumulusConnectionPool thePool = null;
        RecordItem recordItem = null;
        try {
            thePool = getPool(connection);
            result = new DamAsset();
            recordItem = thePool.getRecordItemById(id, false);
            result.name = recordItem.getStringValue(GUID.UID_REC_ASSET_NAME);
            AssetReference assetReference = recordItem.getAssetReferenceValue(GUID.UID_REC_ASSET_REFERENCE);
            //result.data = Utilities.getBytesFromFile(assetReference.getAsset(true).getAsFile());
            // make sure it works with Vault
            result.data = CumulusUtilities.getBytesFromAsset(assetReference.getAsset(false));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (thePool != null && recordItem != null) {
                thePool.releaseReadRecordItem(recordItem);
            }
        }
        return result;
    }

    /**
     * Preview an asset at full size using the Cumulus Pixel Image Converter, used the temp directory
     * @param connection catalog name username and password for connection to Cumulus
     * @param recordId
     * @return
     */
    public byte[] getAssetFullPreview(DamConnectionInfo connection, Integer recordId) {
        return getAssetFullPreview(connection, recordId, tempDir);
    }

    /**
     * Preview an asset at full size using the Cumulus Pixel Image Converter, uses a specified directory (e.g. temp or cache)
     * @param connection catalog name username and password for connection to Cumulus
     * @param recordId
     * @param dir
     * @return
     */
    public byte[] getAssetFullPreview(DamConnectionInfo connection, Integer recordId, File dir) {
        byte[] result = new byte[0];
        RecordItem recordItem = null;
        try {
            recordItem = getPool(connection).getRecordItemById(recordId, false);
            /*
            String convertParameters = "Format=JPEG JPEGQuality=7 StoreIPTC=0 Resolution=72.0";
            File tempFile = recordItem.getAssetReferenceValue(GUID.UID_REC_ASSET_REFERENCE).getAsset(true).convert(dir, convertParameters, GUID.UID_AS_PIXEL_CONVERTER);
            result = Utilities.getBytesFromFile(tempFile);
            tempFile.delete();
             * */
            result = CumulusUtilities.getPreviewData(recordItem);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            getPool(connection).releaseReadRecordItem(recordItem);
        }
        return result;
    }

    /**
     * Return the full asset as a byte array
     * @param connection catalog name username and password for connection to Cumulus
     * @param recordId
     * @return
     */
    public byte[] getAssetData(DamConnectionInfo connection, Integer recordId) {
        byte[] result = new byte[0];
        RecordItem recordItem = null;
        try {
            recordItem = getPool(connection).getRecordItemById(recordId, false);
            InputDataStream is = recordItem.getAssetReferenceValue(GUID.UID_REC_ASSET_REFERENCE).getAsset(true).openInputDataStream();
            try {
                result = Utilities.getBytesFromInputStream(is);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            getPool(connection).releaseReadRecordItem(recordItem);
        }
        return result;
    }

    /**
     * Retrieve the thumbnail for an asset by the preview name.
     * @param connection catalog name username and password for connection to Cumulus
     * @return
     */
    public byte[] getAssetThumbnail(DamConnectionInfo connection, Integer recordId) {
        byte[] result = new byte[0];
        try {
            RecordItem recordItem = getPool(connection).getRecordItemById(recordId, false);
            result = recordItem.getPictureValue(GUID.UID_REC_THUMBNAIL).getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Add a record to a category
     * @param connection
     * @param recordId
     * @param categoryId
     */
    public void attachRecordToCategory(DamConnectionInfo connection, Integer recordId, Integer categoryId) {
        //TODO implement attachRecordToCategory
    }

    /**
     * Remove a record from a category
     * @param connection
     * @param recordId
     * @param categoryId
     */
    public void removeRecordFromCategory(DamConnectionInfo connection, Integer recordId, Integer categoryId) {
        //TODO implement attachRecordToCategory
    }

    /**
     *
     * @param connection
     * @param recordId
     * @param locale
     * @param userComment
     */
    public void addUserComment(DamConnectionInfo connection, Integer recordId, String locale, DamUserComment userComment) {
        //TODO implement addUserComment
    }

    /**
     *
     * @param connection
     * @param recordId
     * @param locale
     * @param userComment
     * @param replyComment
     */
    public void replyToUserComment(DamConnectionInfo connection, Integer recordId, String locale, DamUserComment userComment, DamUserComment replyComment) {
        //TODO implement addUserComment
    }

    /**
     * Retrieve a preview for an asset by the preview name. The previews are stored in a Record Table field, with name and picture columns
     * Previews are specified with a name, and width. Aspect ratio is maintained.
     * @param connection catalog name username and password for connection to Cumulus
     * @param previewFieldDescriptor the Record field that is to be used to store the previews
     * @param previewName name of the preview to retrieve - can be either the name, or name and size (e.g. 'small' or 'small:72' - if the size is included
     * then it can be used to generate the preview if it s missing.
     * @return
     */
    public byte[] getAssetPreviewByName(DamConnectionInfo connection, Integer recordId, String previewName, int compressionLevel, int width, int height) {
        return getAssetPreviewByName(connection, recordId, previewName, compressionLevel, -1, -1, width, height, "jpg", null);
    }

    /**
     * Retrieve a preview for an asset by the preview name. The previews are stored in a Record Table field, with name and picture columns
     * Previews are specified with a name, and width. Aspect ratio is maintained.
     * @param connection catalog name username and password for connection to Cumulus
     * @param previewFieldDescriptor the Record field that is to be used to store the previews
     * @param previewName name of the preview to retrieve - can be either the name, or name and size (e.g. 'small' or 'small:72' - if the size is included
     * @param cacheFile file to store preview in a cache, if not specified, temp file will be created
     * then it can be used to generate the preview if it s missing.
     *
     * As of Daft.lib version 3.2 we no longer store the previews in the catalog, they must live in the cache
     * There is no real value to this except using up catalog space
     * 
     * @return
     */
    public byte[] getAssetPreviewByName(DamConnectionInfo connection, Integer recordId, String previewName, int compressionLevel, int top, int left, int width, int height, String format, File cacheFile) {
        byte[] result = new byte[0];
        RecordItem recordItem = null;
        try {
            String[] nameBits = previewName.split(":");
            if (nameBits.length > 1) {
                try {
                    int size = new Integer(nameBits[1]);
                    result = buildAssetMaxSizePreview(connection, recordId, compressionLevel, size, format, cacheFile);
                } catch (NumberFormatException nfe) {
                    //ignore
                }
            } else {
                if (width > 0 && height > 0) {
                    logger.info("Needed to create preview: '" + previewName + "' for record with id: " + recordId + " in catalog: " + connection.catalogName
                            + " top: " + top + " left: " + left + " width: " + width + " height: " + height + " format " + format);
                    result = buildAssetPreview(connection, recordId, compressionLevel, top, left, width, height, format, cacheFile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            getPool(connection).releaseReadRecordItem(recordItem);
        }
        return result;
    }

    /**
     * Retrieve a preview for an asset by the preview name. The previews are stored in a Record Table field, with name and picture columns
     * Previews are specified with a name, and width. Aspect ratio is maintained.
     * @param connection catalog name username and password for connection to Cumulus
     * @param previewFieldDescriptor the Record field that is to be used to store the previews
     * @param previewName name of the preview to retrieve - can be either the name, or name and size (e.g. 'small' or 'small:72' - if the size is included
     * then it can be used to generate the preview if it s missing.
     * @return
     */
    public byte[] getAssetPreviewByName(DamConnectionInfo connection, Integer recordId, String previewName, String format, int compressionLevel) {
        return getAssetPreviewByName(connection, recordId, previewName, compressionLevel, -1, -1, -1, -1, format, null);
    }

    /**
     * Retrieve a preview for an asset by the preview name. The previews are stored in a Record Table field, with name and picture columns
     * Previews are specified with a name, and width. Aspect ratio is maintained.
     * @param connection catalog name username and password for connection to Cumulus
     * @param previewFieldDescriptor the Record field that is to be used to store the previews
     * @param previewName name of the preview to retrieve - can be either the name, or name and size (e.g. 'small' or 'small:72' - if the size is included
     * @param cacheFile file to store preview in a cache, if not specified, temp file will be created
     * then it can be used to generate the preview if it s missing.
     * @return
     */
    public byte[] getAssetPreviewByName(DamConnectionInfo connection, Integer recordId, String previewName, int compressionLevel, String format, File cacheFile) {
        return getAssetPreviewByName(connection, recordId, previewName, compressionLevel, -1, -1, -1, -1, format, cacheFile);
    }

    /**
     *
     * @param connection
     * @param recordId
     * @param previewName
     * @param compressionLevel
     * @param size
     * @param format
     * @param cacheFile
     * @return
     */
    public byte[] buildAssetMaxSizePreview(DamConnectionInfo connection, Integer recordId, int compressionLevel, int maxSize, String format, File cacheFile) {
        return buildAssetMaxSizePreview(connection, recordId, compressionLevel, maxSize, format, cacheFile, true);
    }

    /**
     * 
     * @param connection
     * @param recordId
     * @param previewName
     * @param compressionLevel
     * @param size
     * @param format
     * @param cacheFile
     * @return
     */
    public byte[] buildAssetMaxSizePreview(DamConnectionInfo connection, Integer recordId, int compressionLevel, int maxSize, String format, File cacheFile, boolean returnData) {
        byte[] result = new byte[0];
        int cl = compressionLevel;
        if (cl < 0 && cl > 10) {
            cl = 10;
        }
        CumulusConnectionPool thePool = null;
        RecordItem recordItem = null;
        try {
            thePool = getPool(connection);
            recordItem = thePool.getRecordItemById(recordId, false);
            ImagingPixmap imagingPixmap = ImagingPixmap.getPixmap(recordItem);
            double rw = new Integer(imagingPixmap.getWidth()).doubleValue();
            double rh = new Integer(imagingPixmap.getHeight()).doubleValue();
            if (rw == 0.0 || rh == 0.0) {
                return result;
            }
            int w = 0;
            int h = 0;
            double aspectRatio = rh / rw;
            if (rw == rh) {
                w = maxSize;
                h = maxSize;
            } else if (rw > rh) {
                w = maxSize;
                h = (int) (new Integer(maxSize).doubleValue() * aspectRatio);
            } else {
                h = maxSize;
                w = (int) (new Integer(maxSize).doubleValue() / aspectRatio);
            }
            BufferedImage img = scalePreview(imagingPixmap, w, h);
            logger.debug("Saving preview to file: " + cacheFile + " for max size: " + maxSize);
            savePreview(cacheFile, format, img);
            //savePreview(cacheFile, format, imagingPixmap);
            if (returnData) {
                result = Utilities.getBytesFromFile(cacheFile);
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        } finally {
            if (thePool != null && recordItem != null) {
                thePool.releaseReadRecordItem(recordItem);
            }
        }
        return result;
    }

    /**
     * Build a preview using a CumulusPreview object
     * Do not return the preview data, this method is just about building the preview
     * If the preview indicates a force is required, and the file exists, it will be deleted
     * If no force, and the file exists, nothing is done
     * @param connection
     * @param recordId
     * @param preview
     * @param cacheFile
     */
    public void storeAssetPreview(DamConnectionInfo connection, Integer recordId, DamPreview preview, File cacheFile) {
        boolean makePreview = false;
        if (cacheFile.exists()) {
            if (preview.isForce()) {
                cacheFile.delete();
                makePreview = true;
            }
        } else {
            makePreview = true;
        }
        if (makePreview) {
            //logger.debug("   --- about to build preview"+preview.getName());
            if (preview.getPreviewType() == DamPreview.Types.MaxSize) {
                buildAssetMaxSizePreview(connection, recordId,
                        preview.getCompressionLevel(), preview.getSize(), preview.getFormatName(),
                        cacheFile, false);
            } else if (preview.getPreviewType() == DamPreview.Types.ScaledBox) {
                buildAssetPreview(connection, recordId,
                        preview.getCompressionLevel(),
                        -1, -1,
                        preview.getWidth(), preview.getHeight(),
                        preview.getFormatName(),
                        cacheFile, false);
            } else if (preview.getPreviewType() == DamPreview.Types.CroppedBox) {
                buildAssetPreview(connection, recordId,
                        preview.getCompressionLevel(),
                        preview.getTop(), preview.getLeft(),
                        preview.getWidth(), preview.getHeight(),
                        preview.getFormatName(),
                        cacheFile, false);
            }
        }
    }

    /**
     *
     * @param connection
     * @param recordId
     * @param width
     * @param height
     * @param cacheFile
     * @param returnData
     * @return
     */
    public byte[] buildAssetPreview(DamConnectionInfo connection, Integer recordId, int compressionLevel, int top, int left, int width, int height, String format, File cacheFile) {
        return buildAssetPreview(connection, recordId, compressionLevel, top, left, width, height, format, cacheFile, true);
    }

    /**
     * 
     * @param connection
     * @param recordId
     * @param width
     * @param height
     * @param cacheFile
     * @param returnData
     * @return
     */
    private byte[] buildAssetPreview(DamConnectionInfo connection, Integer recordId, int compressionLevel, int top, int left, int width, int height, String format, File cacheFile, boolean returnData) {
        byte[] result = new byte[0];
        CumulusConnectionPool thePool = null;
        RecordItemCollection collection = null;
        int cl = compressionLevel;
        if (cl < 0 && cl > 10) {
            cl = 10;
        }
        try {
            boolean fullPreview = (width <= 0 && height <= 0);
            boolean simpleCrop = (!fullPreview && (top >= 0 && left >= 0 && width > 0 && height > 0));
            boolean maxSize = (!fullPreview && !simpleCrop && (width <= 0 || height <= 0));
            thePool = getPool(connection);
            collection = (RecordItemCollection) thePool.borrowObjectToRead(RecordItemCollection.class);
            if (collection != null) {
                RecordItem recordItem = null;
                try {
                    recordItem = collection.getRecordItemByID(recordId);
                } catch (Exception e) {
                    // refresh and try again
                    collection.findAll();
                    recordItem = collection.getRecordItemByID(recordId);
                }

                ImagingPixmap imagingPixmap = ImagingPixmap.getPixmap(recordItem);
                double rw = new Integer(imagingPixmap.getWidth()).doubleValue();
                double rh = new Integer(imagingPixmap.getHeight()).doubleValue();
                if (rw == 0.0 || rh == 0.0) {
                    return result;
                }
                double aspectRatio = rh / rw;
                if (fullPreview) {
                    Raster raster = imagingPixmap.getBufferedImage().getRaster();
                    result = new byte[(int) rw * (int) rh * raster.getNumDataElements()];
                    raster.getDataElements(0, 0, (int) rw, (int) rh, result);
                } else if (maxSize) {
                    // scale the biggest side
                    int w = width;
                    int h = height;
                    if (w < 0) {
                        w = (int) (new Integer(h).doubleValue() * aspectRatio);
                    } else if (h < 0) {
                        h = (int) (new Integer(w).doubleValue() * aspectRatio);
                    }
                    //imagingPixmap.scale(w, h);
                    BufferedImage img = scalePreview(imagingPixmap, w, h);
                    savePreview(cacheFile, format, img);
                    if (returnData) {
                        logger.debug("saving preview to file: " + cacheFile);
                        result = Utilities.getBytesFromFile(cacheFile);
                    }
                } else if (simpleCrop) {
                    logger.debug("doing simple crop");
                    BufferedImage img = cropPreview(imagingPixmap, left, top, width, height);
                    savePreview(cacheFile, format, img);
                    if (returnData) {
                        logger.debug("saving preview to file: " + cacheFile);
                        result = Utilities.getBytesFromFile(cacheFile);
                    }
                } else {
                    // scale the smallest side and then crop
                    int w = width;
                    int h = height;
                    if (rh > rw) {
                        h = (int) (new Integer(w).doubleValue() * aspectRatio);
                    } else if (rh < rw) {
                        w = (int) (new Integer(h).doubleValue() / aspectRatio);
                    }
                    BufferedImage img = scalePreview(imagingPixmap, w, h);
                    //imagingPixmap.scale(w, h);

                    int scaledTop = 0;
                    int scaledLeft = 0;
                    /*
                    if (imagingPixmap.getWidth() > width) {
                    scaledLeft = (imagingPixmap.getWidth() - width) / 2;
                    }
                    if (imagingPixmap.getHeight() > height) {
                    scaledTop = (imagingPixmap.getHeight() - height) / 2;
                    }
                     */
                    if (img.getWidth() > width) {
                        scaledLeft = (img.getWidth() - width) / 2;
                    }
                    if (img.getHeight() > height) {
                        scaledTop = (img.getHeight() - height) / 2;
                    }
                    //imagingPixmap.crop(scaledLeft, scaledTop, width, height);
                    savePreview(cacheFile, format, img.getSubimage(scaledLeft, scaledTop, width, height));
                    if (returnData) {
                        logger.debug("saving preview to file: " + cacheFile);
                        result = Utilities.getBytesFromFile(cacheFile);
                    }
                }
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        } finally {
            if (thePool != null && collection != null) {
                thePool.returnReadObject(collection);
            }
        }
        return result;
    }

    private BufferedImage cropPreview(ImagingPixmap pixmap, int left, int top, int width, int height) throws Exception {
        BufferedImage result = null;
        BufferedImage img = pixmap.getBufferedImage();
        AffineTransform s = new AffineTransform();
        AffineTransformOp sop = new AffineTransformOp(s, AffineTransformOp.TYPE_BICUBIC);
        BufferedImage tImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        sop.filter(pixmap.getBufferedImage(), tImage);
        result = tImage.getSubimage(left, top, width, height);
        return result;

    }

    private BufferedImage scalePreview(ImagingPixmap pixmap, int w, int h) throws Exception {
        BufferedImage result = null;
        BufferedImage img = pixmap.getBufferedImage();
        double wf = new Integer(w).doubleValue() / new Integer(img.getWidth()).doubleValue();
        double hf = new Integer(h).doubleValue() / new Integer(img.getHeight()).doubleValue();
        AffineTransform s = new AffineTransform();
        s.scale(wf, hf);
        AffineTransformOp sop = new AffineTransformOp(s, AffineTransformOp.TYPE_BICUBIC);
        BufferedImage tImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        sop.filter(pixmap.getBufferedImage(), tImage);
        result = tImage;
        return result;
    }

    /**
     * 
     * @param file
     * @param format
     * @param pixmap
     */
    private void savePreview(File file, String format, ImagingPixmap pixmap) {
        boolean ok = false;
        try {
            //BufferedImage bi = pixmap.getBufferedImage();

            // If nor RGB source (e.g. CMYK), need to convert to RGB (or find a way to handle the appropriate model)
            //TODO For previews, RGB should be fine, but must document this is not used for any sort of soft proofing
            //int[] rgbArray = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, bi.getWidth());
            //bi.setRGB(0, 0, bi.getWidth(), bi.getHeight(), rgbArray, 0, bi.getWidth());


            logger.debug("preview buffer height: " + pixmap.getBufferedImage().getHeight());
            if (format == null || "".equals(format)) {
                ok = ImageIO.write(pixmap.getBufferedImage(), "jpg", file);
                //ok = ImageIO.write(bi, "jpg", file);
                logger.debug("default preview for file: " + file.getAbsolutePath() + " saved with status " + ok);
            } else {
                ok = ImageIO.write(pixmap.getBufferedImage(), format.toLowerCase(), file);
                //ok = ImageIO.write(bi, format, file);
                logger.debug(format + " preview for file: " + file.getAbsolutePath() + " saved with status " + ok);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param file
     * @param format
     * @param pixmap
     */
    private void savePreview(File file, String format, BufferedImage bi) {
        boolean ok = false;
        try {
            logger.debug("preview buffer height: " + bi.getHeight());
            if (format == null || "".equals(format)) {
                ok = ImageIO.write(bi, "jpg", file);
                logger.debug("default preview for file: " + file.getAbsolutePath() + " saved with status " + ok);
            } else {
                // ImageIO only works if format name is lowercase, so make sure
                ok = ImageIO.write(bi, format.toLowerCase(), file);
                logger.debug(format + " preview for file: " + file.getAbsolutePath() + " saved with status " + ok);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setFieldValue(Item item, GUID guid, DamFieldValue fieldValue, Layout layout, Locale locale) throws Exception {
        switch (fieldValue.dataType) {
            case FieldTypes.FieldTypeString:
                //TODO sort this out
                // Silently ignore errors - a one off tempory fix for Eniro as they are setting the value
                // of a formula field "Eniro ProductId" directly"
                try {
                    item.setStringValue(guid, fieldValue.stringValue);
                } catch (Exception se) {
                    // do nothing - assume furmula field
                }
                break;
            case FieldTypes.FieldTypeInteger:
                item.setIntValue(guid, fieldValue.integerValue);
                break;
            case FieldTypes.FieldTypeLong:
                item.setLongValue(guid, fieldValue.longValue);
                break;
            case FieldTypes.FieldTypeDouble:
                item.setDoubleValue(guid, fieldValue.doubleValue);
                break;
            case FieldTypes.FieldTypeBool:
                item.setBooleanValue(guid, fieldValue.booleanValue);
                break;
            case FieldTypes.FieldTypePicture:
                item.setPictureValue(guid, new Pixmap(fieldValue.byteArrayValue));
                break;
            case FieldTypes.FieldTypeDate:
                fieldValue.longValue = item.getDateValue(guid).getTime();
                break;
            case FieldTypes.FieldTypeBinary:
                item.setBinaryValue(guid, fieldValue.byteArrayValue);
                break;
            /*
            case FieldTypes.FieldTypeEnum:
            switch (layout.getFieldDefinition(guid).getValueInterpretation()) {
            case FieldTypes.VALUE_INTERPRETATION_DEFAULT:
            result.stringListValue = new CumulusStringListValue[1];
            CumulusStringListValue wsStringListValue = new CumulusStringListValue();
            StringEnumFieldValue stringListValue = item.getStringEnumValue(guid);
            wsStringListValue.id = stringListValue.getID();
            wsStringListValue.displayString = stringListValue.getDisplayString(locale);
            result.stringListValue[0] = wsStringListValue;
            break;
            case FieldTypes.VALUE_INTERPRETATION_STRING_ENUM_MULTIPLE_VALUES:
            FieldDefinition fieldDefinition = layout.getFieldDefinition(guid);
            stringListValue = item.getStringEnumValue(guid);
            Set<Integer> ids = stringListValue.getIDs();
            result.stringListValue = new CumulusStringListValue[ids.size()];
            int c = 0;
            for (Integer id : ids) {
            wsStringListValue = new CumulusStringListValue();
            wsStringListValue.id = id;
            wsStringListValue.displayString = fieldDefinition.getStringEnumName(id, LanguageManager.getCumulusLanguageId(locale));
            result.stringListValue[c++] = wsStringListValue;
            }
            break;
            case FieldTypes.VALUE_INTERPRETATION_STRING_ENUM_LABEL:
            LabelValue wsLabelValue = new LabelValue();
            LabelFieldValue labelValue = item.getLabelValue(guid);
            wsLabelValue.id = labelValue.getID();
            wsLabelValue.displayString = labelValue.getDisplayString(locale);
            wsLabelValue.color = labelValue.getColor();
            result.labelValue = wsLabelValue;
            break;
            default:
            break;
            }
            break;
             */

            case FieldTypes.FieldTypeTable:
                ItemCollection tableCollection = item.getTableValue(guid);
                Layout tableLayout = tableCollection.getLayout();
                Set<GUID> tableGuids = tableLayout.getFieldUIDs();
                /*
                CumulusTableValue wsTableValue = new CumulusTableValue();
                wsTableValue.columnNames = new String[tableGuids.size() - 2];
                int cc = 0;
                for (GUID tableGuid : tableGuids) {
                if (tableGuid.equals(GUID.UID_HOST_ITEM_ID) || tableGuid.equals(GUID.UID_ITEM_ID)) {
                logger.info(" ignoring GUID: " + tableGuid);
                continue;
                }
                wsTableValue.columnNames[cc++] = tableLayout.getFieldDefinition(tableGuid).getName(LanguageManager.getCumulusLanguageId(locale));
                }
                wsTableValue.rows = new CumulusTableItemValue[tableCollection.getItemCount()];
                int tc = 0;
                for (Item tableItem : tableCollection) {
                CumulusTableItemValue tableItemValue = new CumulusTableItemValue();
                tableItemValue.id = tableItem.getID();
                tableItemValue.hostId = tableItem.getIntValue(GUID.UID_HOST_ITEM_ID);
                tableItemValue.columns = new CumulusFieldValue[tableGuids.size() - 2];
                int colc = 0;
                for (GUID tableGuid : tableGuids) {
                if (tableGuid.equals(GUID.UID_HOST_ITEM_ID) || tableGuid.equals(GUID.UID_ITEM_ID)) {
                continue;
                }
                FieldDefinition fieldDefinition = tableLayout.getFieldDefinition(tableGuid);
                CumulusFieldValue fv = getFieldValue(tableItem, tableGuid, tableLayout, locale);
                fv.dataType = fieldDefinition.getFieldType();
                fv.valueInterpretation = fieldDefinition.getValueInterpretation();
                tableItemValue.columns[colc++] = fv;
                }
                wsTableValue.rows[tc++] = tableItemValue;
                }
                 */
                tableCollection.close();
                //result.tableValue = wsTableValue;
                break;
            default:
                break;
        }
    }

    protected DamFieldValue getFieldValue(Item item, GUID guid, Layout layout, Locale locale) throws Exception {
        DamFieldValue result = new DamFieldValue();
        if (item.hasValue(guid)) {
            switch (layout.getFieldDefinition(guid).getFieldType()) {
                case FieldTypes.FieldTypeString:
                    result.stringValue = item.getStringValue(guid);
                    break;
                case FieldTypes.FieldTypeInteger:
                    result.integerValue = item.getIntValue(guid);
                    break;
                case FieldTypes.FieldTypeLong:
                    result.longValue = item.getLongValue(guid);
                    break;
                case FieldTypes.FieldTypeDouble:
                    result.doubleValue = item.getDoubleValue(guid);
                    break;
                case FieldTypes.FieldTypeBool:
                    result.booleanValue = item.getBooleanValue(guid);
                    break;
                case FieldTypes.FieldTypePicture:
                    result.byteArrayValue = item.getPictureValue(guid).getData();
                    break;
                case FieldTypes.FieldTypeDate:
                    result.longValue = item.getDateValue(guid).getTime();
                    break;
                case FieldTypes.FieldTypeBinary:
                    result.byteArrayValue = item.getBinaryValue(guid);
                    break;
                case FieldTypes.FieldTypeEnum:
                    switch (layout.getFieldDefinition(guid).getValueInterpretation()) {
                        case FieldTypes.VALUE_INTERPRETATION_DEFAULT:
                            result.stringListValue = new DamStringListValue[1];
                            DamStringListValue wsStringListValue = new DamStringListValue();
                            StringEnumFieldValue stringListValue = item.getStringEnumValue(guid);
                            wsStringListValue.id = stringListValue.getID();
                            wsStringListValue.displayString = stringListValue.getDisplayString(locale);
                            result.stringListValue[0] = wsStringListValue;
                            result.valueInterpretation = FieldTypes.VALUE_INTERPRETATION_DEFAULT;
                            break;
                        case FieldTypes.VALUE_INTERPRETATION_STRING_ENUM_MULTIPLE_VALUES:
                            FieldDefinition fieldDefinition = layout.getFieldDefinition(guid);
                            stringListValue = item.getStringEnumValue(guid);
                            Set<Integer> ids = stringListValue.getIDs();
                            result.stringListValue = new DamStringListValue[ids.size()];
                            int c = 0;
                            for (Integer id : ids) {
                                wsStringListValue = new DamStringListValue();
                                wsStringListValue.id = id;
                                wsStringListValue.displayString = fieldDefinition.getStringEnumName(id, LanguageManager.getCumulusLanguageId(locale));
                                result.stringListValue[c++] = wsStringListValue;
                            }
                            result.valueInterpretation = FieldTypes.VALUE_INTERPRETATION_STRING_ENUM_MULTIPLE_VALUES;
                            break;
                        case FieldTypes.VALUE_INTERPRETATION_STRING_ENUM_LABEL:
                            DamLabelValue wsLabelValue = new DamLabelValue();
                            LabelFieldValue labelValue = item.getLabelValue(guid);
                            wsLabelValue.id = labelValue.getID();
                            wsLabelValue.displayString = labelValue.getDisplayString(locale);
                            wsLabelValue.color = labelValue.getColor();
                            result.labelValue = wsLabelValue;
                            result.valueInterpretation = FieldTypes.VALUE_INTERPRETATION_STRING_ENUM_LABEL;
                            break;
                        default:
                            result.stringListValue = new DamStringListValue[1];
                            wsStringListValue = new DamStringListValue();
                            stringListValue =
                                    item.getStringEnumValue(guid);
                            wsStringListValue.id = stringListValue.getID();
                            wsStringListValue.displayString = stringListValue.getDisplayString(locale);
                            result.stringListValue[0] = wsStringListValue;
                            result.valueInterpretation = FieldTypes.VALUE_INTERPRETATION_DEFAULT;
                            break;
                    }
                    result.dataType = FieldTypes.FieldTypeEnum;
                    break;

                case FieldTypes.FieldTypeTable:
                    ItemCollection tableCollection = item.getTableValue(guid);
                    Layout tableLayout = tableCollection.getLayout();
                    Set<GUID> tableGuids = tableLayout.getFieldUIDs();
                    DamTableValue wsTableValue = new DamTableValue();
                    wsTableValue.columnNames = new String[tableGuids.size() - 2];
                    int cc = 0;
                    for (GUID tableGuid : tableGuids) {
                        if (tableGuid.equals(GUID.UID_HOST_ITEM_ID) || tableGuid.equals(GUID.UID_ITEM_ID)) {
                            continue;
                        }
                        wsTableValue.columnNames[cc++] = tableLayout.getFieldDefinition(tableGuid).getName(LanguageManager.getCumulusLanguageId(locale));
                    }

                    wsTableValue.rows = new DamTableItemValue[tableCollection.getItemCount()];
                    int tc = 0;
                    for (Item tableItem : tableCollection) {
                        DamTableItemValue tableItemValue = new DamTableItemValue();
                        tableItemValue.id = tableItem.getID();
                        tableItemValue.hostId = tableItem.getIntValue(GUID.UID_HOST_ITEM_ID);
                        tableItemValue.columns = new DamFieldValue[tableGuids.size() - 2];
                        int colc = 0;
                        for (GUID tableGuid : tableGuids) {
                            if (tableGuid.equals(GUID.UID_HOST_ITEM_ID) || tableGuid.equals(GUID.UID_ITEM_ID)) {
                                continue;
                            }
                            FieldDefinition fieldDefinition = tableLayout.getFieldDefinition(tableGuid);
                            DamFieldValue fv = getFieldValue(tableItem, tableGuid, tableLayout, locale);
                            fv.dataType = fieldDefinition.getFieldType();
                            fv.valueInterpretation = fieldDefinition.getValueInterpretation();
                            tableItemValue.columns[colc++] = fv;
                        }
                        wsTableValue.rows[tc++] = tableItemValue;
                    }
                    tableCollection.close();
                    result.tableValue = wsTableValue;
                    break;
                default:
                    result.dataType = FieldTypes.FieldTypeString;
                    result.stringValue = "";
                    break;
            }
        }
        return result;
    }

    /**
     *
     */
    @Override
    public void destroy() {
        try {
            logger.info("eBean shutitng down.");
            super.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Cumulus.CumulusStop();
        }
    }

    /**
     *
     * @param connection catalog name username and password for connection to Cumulus
     * @return
     * @throws java.lang.Exception
     */
    public CumulusConnectionPool getPool(DamConnectionInfo connection) {
        CumulusConnectionPool result = null;
        try {
            result = connectionPools.get(connection.name);
            if (result == null) {
                result = new CumulusConnectionPool();
                boolean ok = result.init(connection);
                if (ok) {
                    connectionPools.put(connection.name, result);
                    logger.info("Created Cumulus connection pool for catalog: '" + connection.name + "' with enterprise server status: " + result.isEnterpriseServer());
                } else {
                    result = null;
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return result;
    }

    /**
     *
     * @param recordItem
     * @param parentCategory
     * @param collection
     * @param categories
     */
    protected void processCategories(RecordItem recordItem, CategoryItem parentCategory, CategoryItemCollection collection, DamCategory[] categories) {
        for (DamCategory category : categories) {
            Set<Integer> categoryIds = new HashSet<Integer>();
            categoryIds = collection.findCategoryIDs(category.name);
            CategoryItem categoryItem = null;
            if (categoryIds == null || categoryIds.size() == 0) {
                // category does not exist, so create it
                categoryItem = parentCategory.createCategoryItem(category.name);
                categoryItem.save();
                categoryIds.add(categoryItem.getID());
            } else if (categoryIds != null && categoryIds.size() > 0) {
                categoryItem = collection.getCategoryItemByID(categoryIds.toArray(new Integer[0])[0]);
            }

            if (category.name == null) {
                // should not happen, ignore
                continue;
            }

            if (!category.name.startsWith("$")) {
                CategoriesFieldValue cfv = recordItem.getCategoriesValue();
                cfv.addIDs(categoryIds);
                recordItem.setCategoriesValue(cfv);
                recordItem.save();
            }

            if (category.subCategories != null || category.subCategories.length > 0) {
                processCategories(recordItem, categoryItem, collection, category.subCategories);
            }
        }
    }

    private DamCategory walkCategoryTree(CategoryItem rootCategory) {
        DamCategory result = new DamCategory();
        result.id = rootCategory.getID();
        result.name = rootCategory.getStringValue(GUID.UID_CAT_NAME);
        ArrayList<DamCategory> subCategories = new ArrayList<DamCategory>();
        CategoryItem childItem = rootCategory.getFirstChildCategoryItem();
        while (childItem != null) {
            //System.out.println("Category: " + childItem.toString());
            DamCategory category = new DamCategory();
            category.id = childItem.getID();
            category.name = childItem.getStringValue(GUID.UID_CAT_NAME);
            subCategories.add(walkCategoryTree(childItem));
            childItem = childItem.getNextSiblingCategoryItem();
        }
        result.subCategories = subCategories.toArray(new DamCategory[subCategories.size()]);
        return result;
    }

    /**
     *
     * @param categoryItem
     * @return
     */
    protected DamCategory buildCategory(CategoryItem categoryItem) {
        DamCategory result = new DamCategory();
        return result;
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     *
     * We don't want to put binary data into JSON (maybe in the future, base64 encode). Also handle returning tables as JSONArrays
     * @param v
     * @param json if true handle returning certain types in a format suitable for JSON
     * @return
     */
    @Override
    public Object getFieldValue(DamFieldValue v, boolean json) {
        return CumulusUtilities.getCumulusFieldValue(v, json);
    }

    @Override
    public DamFieldValue createFieldValue(DamFieldDescriptor fieldDescriptor, String value) throws Exception {
        return CumulusUtilities.createCumulusFieldValue(fieldDescriptor, value);
    }

    /**
     * Class that implements callbacks for Cumulus cataloging
     */
    private class WsCatalogingListener implements CatalogingListener<ItemCollection> {

        public final static int NULL_ID = -1;
        private int itemId = NULL_ID;

        public WsCatalogingListener() {
        }

        public void catalogingStarted(CatalogingEventObject<ItemCollection> event) {
            itemId = NULL_ID;
        }

        @SuppressWarnings("empty-statement")
        public void catalogingFinished(CatalogingEventObject<ItemCollection> event) {
            ;
        }

        @SuppressWarnings("empty-statement")
        public void countingAssets(CatalogingEventObject<ItemCollection> event) {
            ;
        }

        public void assetAdded(CatalogingEventObject<ItemCollection> event) {
            itemId = event.getItemID();
        }

        @SuppressWarnings("empty-statement")
        public void assetUpdated(CatalogingEventObject<ItemCollection> event) {
            ;
        }

        @SuppressWarnings("empty-statement")
        public void assetIgnored(CatalogingEventObject<ItemCollection> event) {
            ;
        }

        @SuppressWarnings("empty-statement")
        public void assetFailed(CatalogingEventObject<ItemCollection> event) {
            ;
        }

        public int getItemId() {
            return itemId;
        }

        public void setItemId(int itemId) {
            this.itemId = itemId;
        }
    }
}

