/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daftsolutions.lib.mcal;

import java.io.File;
import java.util.HashMap;

/**
 *
 * @author colin
 */
public class CentralAssetLocation {

    public File root;

    public HashMap<String, Zone> zones = null;

    public CentralAssetLocation(File root) {
        this.root = root;
        if (!root.exists()) root.mkdirs();
        zones = new HashMap<String, Zone>();
    }

    public File getRoot() {
        return root;
    }

    public void addZone(String name, Zone zone) {
        zones.put(name, zone);
        zone.setCentralAssetLocation(this);
    }

    public void removeZone(String name) {
        zones.remove(name);
    }

    public Zone getZone(String name) {
        return zones.get(name);
    }

    public void addFile(String zoneName, File file) {
        zones.get(zoneName).addFile(file);
    }
}
