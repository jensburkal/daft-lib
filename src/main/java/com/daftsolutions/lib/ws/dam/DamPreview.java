/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daftsolutions.lib.ws.dam;

/**
 *
 * @author colin
 */
public class DamPreview {

    public static enum Types {
        Unknown, MaxSize, ScaledBox, CroppedBox
    };

    public static enum Formats {
        Unknown, Jpg, Png, Gif
    };
    public final static int DEFAUZLT_COMPRESSION_LEVEL = 7;

    private String name = "";
    private Types previewType = Types.Unknown;
    private Formats previewFormat = Formats.Jpg;
    private int compressionLevel = DEFAUZLT_COMPRESSION_LEVEL;
    private boolean force = false;
    private int size = 0;
    private int width = 0;
    private int height = 0;
    private int top = 0;
    private int left = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCompressionLevel() {
        return compressionLevel;
    }

    public void setCompressionLevel(int compressionLevel) throws Exception {
        if (compressionLevel > 0 && compressionLevel <= 10) {
            this.compressionLevel = compressionLevel;
        }
        else {
            throw new Exception("Invalid compression level (not in range 1-10: "+compressionLevel);
        }
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public Formats getPreviewFormat() {
        return previewFormat;
    }

    public void setPreviewFormat(Formats previewFormat) {
        this.previewFormat = previewFormat;
    }

    public Types getPreviewType() {
        return previewType;
    }

    public void setPreviewType(Types previewType) {
        this.previewType = previewType;
    }

    public boolean setTypeByName(String name) {
        boolean result = true;
        try {
            previewType = Types.valueOf(name);
        }
        catch (Exception e) {
            result = false;
        }
        return result;
    }

    public boolean setFormatByName(String name) {
        boolean result = true;
        try {
            previewFormat = Formats.valueOf(name);
        }
        catch (Exception e) {
            result = false;
        }
        return result;
    }

    public String getFormatName() {
        return previewFormat.toString();
    }

    public String getTypeName() {
        return previewType.toString();
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }


}
