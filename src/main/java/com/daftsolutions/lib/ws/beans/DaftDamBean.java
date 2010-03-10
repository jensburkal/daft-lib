package com.daftsolutions.lib.ws.beans;

import com.daftsolutions.lib.pool.CumulusConnectionPool;
import com.daftsolutions.lib.ws.dam.DamAsset;
import com.daftsolutions.lib.ws.dam.DamCategory;
import com.daftsolutions.lib.ws.dam.DamConnectionInfo;
import com.daftsolutions.lib.ws.dam.DamFieldDescriptor;
import com.daftsolutions.lib.ws.dam.DamFieldValue;
import com.daftsolutions.lib.ws.dam.DamPreview;
import com.daftsolutions.lib.ws.dam.DamRecord;
import com.daftsolutions.lib.ws.dam.DamRecordCollection;
import com.daftsolutions.lib.ws.dam.DamRecordLock;
import com.daftsolutions.lib.ws.dam.DamResultStatus;
import com.daftsolutions.lib.ws.dam.DamUser;
import com.daftsolutions.lib.ws.dam.DamUserComment;
import java.io.File;
import java.util.Properties;

/**
 *
 * @author colinmanning
 */
public class DaftDamBean extends DamBean {

    public final static String NAME = "daftdam";

    public DaftDamBean() {
        super();
    }

    @Override
    public void terminate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init(Properties properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamFieldValue getRecordField(DamConnectionInfo connection, Integer recordId, String fieldGuid, String locale) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamResultStatus setRecordField(DamConnectionInfo connection, Integer recordId, String fieldGuid, DamFieldValue fieldValue, String locale) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamResultStatus updateRecord(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, DamRecord record, DamCategory[] categories, String locale) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamResultStatus[] updateRecords(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, DamRecord[] records, DamCategory[] categories, String locale) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Integer findRecord(DamConnectionInfo connection, String query, String locale) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamRecordCollection findRecordsByQuickSearch(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, String quickSearch, Integer offset, Integer count, String locale) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamRecordCollection findRecords(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, String query, Integer offset, Integer count, String locale) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamRecord getRecordById(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, Integer recordId, String locale) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamCategory getCategoryById(DamConnectionInfo connection, DamFieldDescriptor[] fieldDescriptors, Integer categoryId, String locale) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamCategory findCategories(DamConnectionInfo connection, String categoryPath) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamUser getUser(DamConnectionInfo connection, DamConnectionInfo testConnection) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamFieldDescriptor[] getCatalogRecordFieldDescriptors(DamConnectionInfo connection, String[] fieldNames) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamFieldDescriptor[] getCatalogAllRecordFieldDescriptors(DamConnectionInfo connection) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamFieldDescriptor[] getCatalogCategoryFieldDescriptors(DamConnectionInfo connection, String[] fieldNames) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteAsset(DamConnectionInfo connection, Integer id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteRecord(DamConnectionInfo connection, Integer id, boolean deleteAsset) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteRecords(DamConnectionInfo connection, Integer[] ids, boolean deleteAssets) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteAssets(DamConnectionInfo connection, Integer[] ids) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteCategory(DamConnectionInfo connection, Integer id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteCategories(DamConnectionInfo connection, Integer[] ids) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamRecord[] createAssetsWithData(DamConnectionInfo connection, DamAsset[] assets, DamCategory[] categories, String assetHandlingSet, DamFieldDescriptor[] fieldDescriptors, DamFieldValue[] fieldValues, String locale) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamCategory createCategoryFromPath(DamConnectionInfo connection, String path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamCategory createSubCategory(DamConnectionInfo connection, Integer id, String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamResultStatus addRecordToCategory(DamConnectionInfo connection, int categoryId, int recordId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamRecord createAssetWithData(DamConnectionInfo connection, DamAsset asset, File file, DamCategory[] categories, String assetHandlingSet, DamFieldDescriptor[] fieldDescriptors, DamFieldValue[] fieldValues, String locale) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamRecord[] createAssets(DamConnectionInfo connection, DamAsset[] assets, DamCategory[] categories, String assetHandlingSet) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamAsset[] downloadAssets(DamConnectionInfo connection, DamRecord[] records, String assetAction) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamAsset downloadAsset(DamConnectionInfo connection, int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getAssetFullPreview(DamConnectionInfo connection, Integer recordId, File dir) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getAssetData(DamConnectionInfo connection, Integer recordId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getAssetThumbnail(DamConnectionInfo connection, Integer recordId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void attachRecordToCategory(DamConnectionInfo connection, Integer recordId, Integer categoryId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeRecordFromCategory(DamConnectionInfo connection, Integer recordId, Integer categoryId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addUserComment(DamConnectionInfo connection, Integer recordId, String locale, DamUserComment userComment) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void replyToUserComment(DamConnectionInfo connection, Integer recordId, String locale, DamUserComment userComment, DamUserComment replyComment) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getAssetPreviewByName(DamConnectionInfo connection, Integer recordId, String previewName, int compressionLevel, int width, int height, int rotateQuadrant) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getAssetPreviewByName(DamConnectionInfo connection, Integer recordId, String previewName, int compressionLevel, int top, int left, int width, int height, int rotateQuadrant, String format, File cacheFile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getAssetPreviewByName(DamConnectionInfo connection, Integer recordId, String previewName, int rotateQuadrant, String format, int compressionLevel) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getAssetPreviewByName(DamConnectionInfo connection, Integer recordId, String previewName, int compressionLevel, int rotateQuadrant, String format, File cacheFile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] buildAssetMaxSizePreview(DamConnectionInfo connection, Integer recordId, int compressionLevel, int maxSize, int rotateQuadrant, String format, File cacheFile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] buildAssetMaxSizePreview(DamConnectionInfo connection, Integer recordId, int compressionLevel, int maxSize, int rotateQuadrant, String format, File cacheFile, boolean returnData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void storeAssetPreview(DamConnectionInfo connection, Integer recordId, DamPreview preview, File cacheFile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] buildAssetPreview(DamConnectionInfo connection, Integer recordId, int compressionLevel, int top, int left, int width, int height, int rotateQuadrant, String format, File cacheFile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CumulusConnectionPool getPool(DamConnectionInfo connection) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Object getFieldValue(DamFieldValue v, boolean json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamFieldValue createFieldValue(DamFieldDescriptor fieldDescriptor, String value) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean makeAssetVariant(Integer parentId, Integer recordId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] buildAssetRotatePreview(DamConnectionInfo connection, Integer recordId, int compressionLevel, int quadrant, String format, File cacheFile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] buildAssetRotatePreview(DamConnectionInfo connection, Integer recordId, int compressionLevel, int quadrant, String format, File cacheFile, boolean returnData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamRecord createAssetVariant(DamConnectionInfo connection, int parentId, DamAsset asset, String assetHandlingSet) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamRecordLock lockAsset(DamConnectionInfo connection, int id, String userName, String comment, boolean doLog) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DamRecordLock unlockAsset(DamConnectionInfo connection, int id, String userName, String comment, boolean doLog) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void logEvent(DamConnectionInfo connection, int recordId, LogEvents event, String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
