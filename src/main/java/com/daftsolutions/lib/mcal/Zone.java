/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daftsolutions.lib.mcal;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 *
 * @author colin
 */
public class Zone {

    public final static String FOLDER_PREFIX = "folder_";
    private int DEFAULT_MAX_FILES = 10;
    private CentralAssetLocation centralAssetLocation = null;
    private File zoneRoot = null;
    private List<File> folders = null;
    private int maxFiles = DEFAULT_MAX_FILES;
    private int folderCount = -1;
    private File currentFolder = null;
    private int currentFolderFileCount = 0;

    public Zone(CentralAssetLocation cal, String name, File root) {
        zoneRoot = new File(cal.getRoot(), root.getName());
        folders = new ArrayList<File>();
        cal.addZone(name, this);
        // check the folder count
        currentFolder = new File(zoneRoot, nextZoneFolder());
    }

    public void setRoot(File root) {
        this.zoneRoot = root;
    }

    public void setCentralAssetLocation(CentralAssetLocation centralAssetLocation) {
        this.centralAssetLocation = centralAssetLocation;
    }

    public int getMaxFiles() {
        return maxFiles;
    }

    public void setMaxFiles(int maxFiles) {
        if (maxFiles < DEFAULT_MAX_FILES) {
            return;
        }
        this.maxFiles = maxFiles;
    }

    public void addFile(File file) {
        checkZoneStatus();
        // copy the file
        currentFolderFileCount++;
    }

    public void addFile(String fileName, byte[] data) {
        checkZoneStatus();
        // copy the file
        currentFolderFileCount++;
    }

    private void checkZoneStatus() {
        if ((currentFolderFileCount = currentFolder.listFiles().length) > maxFiles) {
            currentFolder = new File(zoneRoot, nextZoneFolder());
            currentFolderFileCount = 0;
        }
    }

    private String nextZoneFolder() {
        return String.format("%07d", folderCount++);
    }

}
