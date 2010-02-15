/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daftsolutions.lib.ws.beans;

import com.daftsolutions.lib.mcal.CentralAssetLocation;
import com.daftsolutions.lib.pool.CumulusConnectionPool;
import com.daftsolutions.lib.ws.dam.DamAsset;
import com.daftsolutions.lib.ws.dam.DamCategory;
import com.daftsolutions.lib.ws.dam.DamConnectionInfo;
import com.daftsolutions.lib.ws.dam.DamFieldDescriptor;
import com.daftsolutions.lib.ws.dam.DamFieldValue;
import com.daftsolutions.lib.ws.dam.DamPreview;
import com.daftsolutions.lib.ws.dam.DamRecord;
import com.daftsolutions.lib.ws.dam.DamRecordCollection;
import com.daftsolutions.lib.ws.dam.DamResultStatus;
import com.daftsolutions.lib.ws.dam.DamUser;
import com.daftsolutions.lib.ws.dam.DamUserComment;
import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author colinmanning
 */
public abstract class DamBean {

    public final static String VERSION_NUMBER = "3.6.1";
    // constants
    public final static String TEMP_DIR = "temp-dir";
    public final static String ENABLE_CACHE = "enable-cache";
    public final static String CACHE_DIR = "cache-dir";
    public final static String TRUE = "true";
    public final static String FALSE = "false";
    public final static String FIELD_CATEGORY_NAME = "Category Name";
    // various stuff
    protected File tempDir = null;
    protected Properties properties = null;
    protected File cacheDir = null;
    protected Map<String, CentralAssetLocation> mcals = null;

    /**
     * An abstract class that rpovides access to a Digital Asset Management system, typically for web service based access.
     * Override this class to provide support for any DAM solution via the Daft latform.
     */
    public DamBean() {
    }

    /**
     * Provide a name for the digital asset management system, to be used in RESTful webservice calls to identify the DAM.
     * Als tis name can be used in a web application configuration to identify this DAM system as the default
     * @return
     */
    public abstract String getName();

    /**
     * Close down the DAM Bean, releasing reources etc.
     */
    public abstract void terminate();

    /**
     * Initialise the DAM, with properties provided externally
     */
    public abstract void init(Properties properties);

    public Object getProperty(String propertyKey) {
        return properties.getProperty(propertyKey);
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Return the temporary directory used when processing files etc.
     * @return
     */
    public File getTempDir() {
        return tempDir;
    }

    /**
     *
     * @param tempDir
     */
    public void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }

    /**
     *
     * @param connection catalog name username and password for connection to the DAM system
     * @param fieldDescriptor
     * @param recordId
     * @param fieldGuid
     * @param locale
     * @param username
     * @param password
     * @return
     */
    public abstract DamFieldValue getRecordField(DamConnectionInfo connection, Integer recordId, String fieldGuid, String locale);

    /**
     *
     * @param connection catalog name username and password for connection to the DAM system
     * @param fieldDescriptor
     * @param record
     * @param locale
     * @param username
     * @param password
     * @return
     */
    public abstract DamResultStatus setRecordField(DamConnectionInfo connection, Integer recordId, String fieldGuid, DamFieldValue fieldValue, String locale);

    /**
     *
     * @param connection
     * @param fieldDescriptors
     * @param record
     * @param categories
     * @param locale
     * @return
     */
    public abstract DamResultStatus updateRecord(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, DamRecord record, DamCategory[] categories, String locale);

    /**
     *
     * @param connection
     * @param fieldDescriptors
     * @param records
     * @param categories
     * @param locale
     * @return
     */
    public abstract DamResultStatus[] updateRecords(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, DamRecord[] records, DamCategory[] categories, String locale);

    /**
     * Return the id of a single record. If query returns more than one, only the id of the first is returned
     * @param connection
     * @param query
     * @param locale
     * @return
     */
    public abstract Integer findRecord(DamConnectionInfo connection, String query, String locale);

    public abstract DamRecordCollection findRecordsByQuickSearch(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, String quickSearch, Integer offset, Integer count, String locale);

    /**
     *
     * @param connection catalog name username and password for connection to the DAM system
     * @param fieldDescriptors
     * @param query
     * @param offset
     * @param count
     * @param locale
     * @param username
     * @param password
     * @return
     */
    public abstract DamRecordCollection findRecords(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, String query, Integer offset, Integer count, String locale);

    /**
     *
     * @param connection catalog name username and password for connection to the DAM system
     * @param fieldDescriptors
     * @param recordId
     * @param locale
     * @param username
     * @param password
     * @return
     */
    public abstract DamRecord getRecordById(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, Integer recordId, String locale);

    /**
     *
     * @param connection catalog name username and password for connection to the DAM system
     * @param fieldDescriptors
     * @param categoryId
     * @param locale
     * @param username
     * @param password
     * @return
     */
    public abstract DamCategory getCategoryById(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, Integer categoryId, String locale);

    /**
     * @param connection catalog name username and password for connection to the DAM system
     * @param fieldGuids
     * @param query
     * @param locale
     * @param root
     * @param recursive
     * @return
     */
    public abstract DamCategory findCategories(DamConnectionInfo connection, String categoryPath);

    /**
     * Returns information about a user, based on credentials in the specified connection
     * Hard coding of Cumulus user field names here for now
     * @param connection server, host, username, password and catalog to check
     * @return a CumulusUser object if the credentials can be validated and access is available for the catalog, otherwise null
     */
    public abstract DamUser getUser(DamConnectionInfo connection, DamConnectionInfo testConnection);

    /**
     *
     * @param connection catalog name username and password for connection to the DAM system
     * @param fieldNames
     * @return
     */
    public abstract DamFieldDescriptor[] getCatalogRecordFieldDescriptors(DamConnectionInfo connection, String[] fieldNames);

    /**
     *
     * @param connection catalog name username and password for connection to the DAM system
     * @param fieldNames
     * @return
     */
    public abstract DamFieldDescriptor[] getCatalogAllRecordFieldDescriptors(DamConnectionInfo connection);

    /**
     *
     * @param connection catalog name username and password for connection to the DAM system
     * @param fieldNames
     * @return
     */
    public abstract DamFieldDescriptor[] getCatalogCategoryFieldDescriptors(DamConnectionInfo connection, String[] fieldNames);

    /**
     *
     * @param connection
     * @param id
     */
    public abstract void deleteAsset(DamConnectionInfo connection, Integer id);

    /**
     *
     * @param connection
     * @param id
     * @param deleteAsset
     */
    public abstract void deleteRecord(DamConnectionInfo connection, Integer id, boolean deleteAsset);

    /**
     *
     * @param connection
     * @param ids
     * @param deleteAssets
     */
    public abstract void deleteRecords(DamConnectionInfo connection, Integer[] ids, boolean deleteAssets);

    /**
     *
     * @param connection
     * @param ids
     */
    public abstract void deleteAssets(DamConnectionInfo connection, Integer[] ids);

    /**
     *
     * @param connection
     * @param id
     * @return
     */
    public abstract void deleteCategory(DamConnectionInfo connection, Integer id);

    /**
     *
     * @param connection
     * @param ids
     * @return
     */
    public abstract void deleteCategories(DamConnectionInfo connection, Integer[] ids);

    /**
     *
     * @param connection catalog name username and password for connection to the DAM system
     * @param assets
     * @param assetHandlingSet
     * @param fieldDescriptors
     * @param fieldValues
     * @paral locale
     * @param username
     * @param password
     * @return
     */
    public abstract DamRecord[] createAssetsWithData(DamConnectionInfo connection, DamAsset[] assets, DamCategory[] categories, String assetHandlingSet,
            DamFieldDescriptor[] fieldDescriptors, DamFieldValue[] fieldValues, String locale);

    /**
     *
     * @param connection
     * @param path
     * @return
     */
    public abstract DamCategory createCategoryFromPath(DamConnectionInfo connection, String path);

    /**
     *
     * @param connection
     * @param id parent category id
     * @param name new category name
     * @return
     */
    public abstract DamCategory createSubCategory(DamConnectionInfo connection, Integer id, String name);

    public abstract DamResultStatus addRecordToCategory(DamConnectionInfo connection, int categoryId, int recordId);

    /**
     *
     * @param connection catalog name username and password for connection to the DAM system
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
    public abstract DamRecord createAssetWithData(DamConnectionInfo connection, DamAsset asset, File file, DamCategory[] categories, String assetHandlingSet,
            DamFieldDescriptor[] fieldDescriptors, DamFieldValue[] fieldValues, String locale);

    /**
     *
     * @param connection
     * @param assets
     * @param assetHandlingSet
     * @return
     */
    public abstract DamRecord[] createAssets(DamConnectionInfo connection, DamAsset[] assets, DamCategory[] categories, String assetHandlingSet);

    /**
     *
     * @param connection catalog name username and password for connection to the DAM system
     * @param records
     * @param username
     * @param password
     * @return
     */
    public abstract DamAsset[] downloadAssets(DamConnectionInfo connection, DamRecord[] records, String assetAction);

    /**
     *
     * @param connection catalog name username and password for connection to the DAM system
     * @param id
     * @return
     */
    public abstract DamAsset downloadAsset(DamConnectionInfo connection, int id);

    /**
     * Preview an asset at full size using the Cumulus Pixel Image Converter, used the temp directory
     * @param connection catalog name username and password for connection to the DAM system
     * @param recordId
     * @return
     */
    public abstract byte[] getAssetFullPreview(DamConnectionInfo connection, Integer recordId);

    /**
     * Preview an asset at full size using the Cumulus Pixel Image Converter, uses a specified directory (e.g. temp or cache)
     * @param connection catalog name username and password for connection to the DAM system
     * @param recordId
     * @param dir
     * @return
     */
    public abstract byte[] getAssetFullPreview(DamConnectionInfo connection, Integer recordId, File dir);

    /**
     * Return the full asset as a byte array
     * @param connection catalog name username and password for connection to the DAM system
     * @param recordId
     * @return
     */
    public abstract byte[] getAssetData(DamConnectionInfo connection, Integer recordId);

    /**
     * Retrieve the thumbnail for an asset by the preview name.
     * @param connection catalog name username and password for connection to the DAM system
     * @return
     */
    public abstract byte[] getAssetThumbnail(DamConnectionInfo connection, Integer recordId);

    /**
     * Add a record to a category
     * @param connection
     * @param recordId
     * @param categoryId
     */
    public abstract void attachRecordToCategory(DamConnectionInfo connection, Integer recordId, Integer categoryId);

    /**
     * Remove a record from a category
     * @param connection
     * @param recordId
     * @param categoryId
     */
    public abstract void removeRecordFromCategory(DamConnectionInfo connection, Integer recordId, Integer categoryId);

    /**
     *
     * @param connection
     * @param recordId
     * @param locale
     * @param userComment
     */
    public abstract void addUserComment(DamConnectionInfo connection, Integer recordId, String locale, DamUserComment userComment);

    /**
     *
     * @param connection
     * @param recordId
     * @param locale
     * @param userComment
     * @param replyComment
     */
    public abstract void replyToUserComment(DamConnectionInfo connection, Integer recordId, String locale, DamUserComment userComment, DamUserComment replyComment);

    /**
     * Retrieve a preview for an asset by the preview name. The previews are stored in a Record Table field, with name and picture columns
     * Previews are specified with a name, and width. Aspect ratio is maintained.
     * @param connection catalog name username and password for connection to the DAM system
     * @param previewFieldDescriptor the Record field that is to be used to store the previews
     * @param previewName name of the preview to retrieve - can be either the name, or name and size (e.g. 'small' or 'small:72' - if the size is included
     * then it can be used to generate the preview if it s missing.
     * @return
     */
    public abstract byte[] getAssetPreviewByName(DamConnectionInfo connection, Integer recordId, String previewName, int compressionLevel, int width, int height);

    /**
     * Retrieve a preview for an asset by the preview name. The previews are stored in a Record Table field, with name and picture columns
     * Previews are specified with a name, and width. Aspect ratio is maintained.
     * @param connection catalog name username and password for connection to the DAM system
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
    public abstract byte[] getAssetPreviewByName(DamConnectionInfo connection, Integer recordId, String previewName, int compressionLevel, int top, int left, int width, int height, String format, File cacheFile);

    /**
     * Retrieve a preview for an asset by the preview name. The previews are stored in a Record Table field, with name and picture columns
     * Previews are specified with a name, and width. Aspect ratio is maintained.
     * @param connection catalog name username and password for connection to the DAM system
     * @param previewFieldDescriptor the Record field that is to be used to store the previews
     * @param previewName name of the preview to retrieve - can be either the name, or name and size (e.g. 'small' or 'small:72' - if the size is included
     * then it can be used to generate the preview if it s missing.
     * @return
     */
    public abstract byte[] getAssetPreviewByName(DamConnectionInfo connection, Integer recordId, String previewName, String format, int compressionLevel);

    /**
     * Retrieve a preview for an asset by the preview name. The previews are stored in a Record Table field, with name and picture columns
     * Previews are specified with a name, and width. Aspect ratio is maintained.
     * @param connection catalog name username and password for connection to the DAM system
     * @param previewFieldDescriptor the Record field that is to be used to store the previews
     * @param previewName name of the preview to retrieve - can be either the name, or name and size (e.g. 'small' or 'small:72' - if the size is included
     * @param cacheFile file to store preview in a cache, if not specified, temp file will be created
     * then it can be used to generate the preview if it s missing.
     * @return
     */
    public abstract byte[] getAssetPreviewByName(DamConnectionInfo connection, Integer recordId, String previewName, int compressionLevel, String format, File cacheFile);

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
    public abstract byte[] buildAssetMaxSizePreview(DamConnectionInfo connection, Integer recordId, int compressionLevel, int maxSize, String format, File cacheFile);

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
    public abstract byte[] buildAssetMaxSizePreview(DamConnectionInfo connection, Integer recordId, int compressionLevel, int maxSize, String format, File cacheFile, boolean returnData);

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
    public abstract void storeAssetPreview(DamConnectionInfo connection, Integer recordId, DamPreview preview, File cacheFile);

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
    public abstract byte[] buildAssetPreview(DamConnectionInfo connection, Integer recordId, int compressionLevel, int top, int left, int width, int height, String format, File cacheFile);

    /**
     *
     */
    public void destroy() {
        try {
            terminate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    /**
     *
     * @param connection catalog name username and password for connection to the DAM system
     * @return
     * @throws java.lang.Exception
     */
    public abstract CumulusConnectionPool getPool(DamConnectionInfo connection);

    /**
     *
     * @param locale
     * @return
     */
    public Locale getLocale(String locale) {
        return (locale != null && !"".equals(locale)) ? new Locale(locale) : Locale.getDefault();
    }

    /**
     *
     * We don't want to put binary data into JSON (maybe in the future, base64 encode). Also handle returning tables as JSONArrays
     * @param v
     * @param json if true handle returning certain types in a format suitable for JSON
     * @return
     */
    public abstract Object getFieldValue(DamFieldValue v, boolean json);

        /**
     * Sets values from a string value, e.g. from a html form input field or JSON
     *
     * @param fieldDescriptor
     * @param value
     * @return
     */
    public abstract DamFieldValue createFieldValue(DamFieldDescriptor fieldDescriptor, String value) throws Exception;

}
