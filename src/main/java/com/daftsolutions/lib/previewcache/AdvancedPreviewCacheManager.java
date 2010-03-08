package com.daftsolutions.lib.previewcache;

import java.io.File;

/**
 * A preview cache manager with finer grained directory structure, better for handling catalogs with very many assets
 * Previews are stored based on their id, with a multi-level directure structure
 * Up to four levels - 00/00/00/00, each with previews for up to 100 record ids, supports 100 million records per catalog - more than enough
 * @author colin
 */
public class AdvancedPreviewCacheManager extends PreviewCacheManager {

    public AdvancedPreviewCacheManager(File cacheDir) {
        super(cacheDir);
    }

    /**
     * Looks at the record id, and calculates the directory structure based on the max id and folder level values set for the cache
     * @param recordId
     * @return
     */
    @Override
    protected String makePartitionName(String recordId) {
        return mapIdToPath(new Integer(recordId));
    }

    private String mapIdToPath(int id) {
        // drop the last 2 digits, not part of folder structure
        int base = id / 100;
        int l[] = new int[4];
        for (int i=3;i>=0;i--) {
            l[i] = base % 100;
            base = base / 100;
        }
        return String.format("%02d/%02d/%02d/%02d", l[0], l[1], l[2], l[3]);
    }

    /**
     * Return a file that will be used to store a preview for an asset
     * do not put the field id in the path - as we use the id all the time
     * @param id the Cumulus record id
     * @return
     */
    @Override
    public File makeCachePath(String catalogName, String fieldId, String recordId) {
        String cacheLocation = catalogName + "/" + makePartitionName(recordId);
        File cachePath = new File(cacheDir, cacheLocation);
        if (!cachePath.exists()) {
            cachePath.mkdirs();
        }
        return cachePath;
    }
}
