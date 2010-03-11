/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daftsolutions.lib.previewcache;

import com.daftsolutions.lib.utils.Utilities;
import java.io.File;
import java.io.FilenameFilter;

/**
 * Provide various functions to anageg the Preview Cache
 * @author colin
 */
public class PreviewCacheManager {

    protected File cacheDir = null;

    public PreviewCacheManager(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    public File getCacheDir() {
        return cacheDir;
    }

    /**
     * Returns a file to be used for a preview, and if it exists, and force is true, then delete the existing file if possible
     * @param catalogName
     * @param fieldId
     * @param recordId
     * @param previewName
     * @param force
     * @return
     */
    public File makeCacheFile(String catalogName, String fieldId, String recordId, String previewName, boolean force) {
        File cachePath = makeCachePath(catalogName, fieldId, recordId);
        return makeCacheFile(cachePath, recordId, previewName, force);
    }

    /**
     * Returns a file to be used for a preview, and if it exists, and force is true, then delete the existing file if possible
     * @param cachePath
     * @param recordId
     * @param previewName
     * @param force
     * @return
     */
    public File makeCacheFile(File cachePath, String recordId, String previewName, boolean force) {
        File result = new File(cachePath, recordId + "_" + previewName.split(":")[0]);
        try {
            if (force && result.exists()) {
                result.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Return a file that will be used to store a preview for an asset
     * @param id the Cumulus record id
     * @return
     */
    public File makeCachePath(String catalogName, String fieldId, String recordId) {
        // The cache file will be stored in the cache at location "catalogName/fieldId//id[0..1]/id/previewName
        // make sure previewName does not contain a size (e.g. small:50)
        // cache folder is up to 4 characters long, made up - if id is ABCDEFGH, cache folder will be HAGB - to randomise it a bit
        String cacheLocation = catalogName + "/" + makePartitionName(recordId) + "/" + fieldId;
        File cachePath = new File(cacheDir, cacheLocation);
        if (!cachePath.exists()) {
            cachePath.mkdirs();
        }
        return cachePath;
    }

    /**
     * Return the actual bytes form a preview file
     * @param file
     * @return
     * @throws Exception
     */
    public byte[] getPreviewData(File file) throws Exception {
        return Utilities.getBytesFromFile(file);
    }

    /**
     * Remove all files in a cache path for a given record id, or just one preview if a preview name is provided
     * @param cachePath
     * @param recordId
     * @param cachePreviewName
     * @throws Exception
     */
    public void clearPathForRecord(File cachePath, String recordId, String previewName) throws Exception {
        if (previewName == null) {
            File[] clearFiles = cachePath.listFiles(new IdFilenameFilter(recordId));
            for (File clearFile : clearFiles) {
                clearFile.delete();
            }
        } else {
            File result = new File(cachePath, recordId + "_" + previewName);
            try {
                if (result.exists()) {
                    result.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected String makePartitionName(String recordId) {
        String result = null;
        // The cache file will be stored in the cache at location "catalogName/fieldId//id[0..1]/id/previewName
        // make sure previewName does not contain a size (e.g. small:50)
        // cache folder is up to 4 characters long, made up - if id is ABCDEFGH, cache folder will be HAGB - to randomise it a bit
        result = "0000"; // default for ids with less than 4 characters
        char[] bits = recordId.toCharArray();
        char[] pbits = bits.length < 4 ? new char[bits.length] : new char[4];
        if (pbits.length == 4) {
            pbits[0] = bits[bits.length - 1];
            pbits[1] = bits[0];
            pbits[2] = bits[bits.length - 2];
            pbits[3] = bits[1];
            result = new String(pbits);
        }
        return result;
    }

    protected class IdFilenameFilter implements FilenameFilter {

        private String id;

        public IdFilenameFilter(String id) {
            this.id = id;
        }

        public boolean accept(File dir, String name) {
            return (name.startsWith(id) ? true : false);
        }
    }
}
