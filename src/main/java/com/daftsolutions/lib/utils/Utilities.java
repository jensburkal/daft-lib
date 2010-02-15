/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daftsolutions.lib.utils;

import com.daftsolutions.lib.ws.dam.DamAsset;
import com.daftsolutions.lib.ws.dam.DamFieldDescriptor;
import com.daftsolutions.lib.ws.dam.DamFieldValue;
import com.daftsolutions.lib.ws.dam.DamLabelValue;
import com.daftsolutions.lib.ws.dam.DamStringListValue;
import com.daftsolutions.lib.ws.dam.DamTableValue;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author colin
 */
public class Utilities {

    private static Logger logger = Logger.getLogger(Utilities.class);
    public final static int BUFFER_SIZE = 1024;

    /**
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            throw new IOException("File is too large " + file.getName());
        }
        return getBytesFromInputStream(is);
    }

    public static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        int count = 0;
        int totalCount = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        ArrayList<byte[]> dataBits = new ArrayList<byte[]>();
        while (true) {
            count = is.read(buffer);
            if (count == 0) {
                continue;
            } else if (count == -1) {
                break;
            }
            // Java 6 specific
            //dataBits.add(Arrays.copyOf(buffer, count));
            dataBits.add(buffer.clone());
            totalCount += count;
        }
        is.close();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(totalCount);
        for (byte[] dataBit : dataBits) {
            bos.write(dataBit, 0, dataBit.length);
        }
        return bos.toByteArray();
    }

    public static DamAsset buildAsset(File file) throws Exception {
        return buildAsset(file.getName(), new FileInputStream(file));
    }

    public static DamAsset buildAsset(String name, InputStream inputStream) {
        DamAsset result = new DamAsset();
        try {
            result.name = name;
            result.data = getBytesFromInputStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Merge any number of arrays of the same type
     *
     * @param <T>
     * @param arrays
     * @return
     */
    public static <T> T[] arrayMerge(T[]... arrays) {
        // Determine required size of new array
        int count = 0;
        for (T[] array : arrays) {
            count += array.length;
        }

        // create new array of required class
        T[] mergedArray = (T[]) Array.newInstance(arrays[0][0].getClass(), count);

        // Merge each array into new array
        int start = 0;
        for (T[] array : arrays) {
            System.arraycopy(array, 0, mergedArray, start, array.length);
            start += array.length;
        }
        return (T[]) mergedArray;
    }

    /**
     * Merge any number of arrays of the same type
     *
     * @param <T>
     * @param arrays
     * @return
     */
    public static <T> T[] mergeArrays(T[]... arrays) {
        List list = new ArrayList();
        for (T[] array : arrays) {
            list.addAll(Arrays.asList(array));
        }
        return (T[]) Array.newInstance(arrays[0][0].getClass(), list.size());
    }

    public final static String JSON_BINARY_VALUE = "<binary>";
    public final static String JSON_PICTURE_VALUE = "<picture>";

    public static boolean isTrue(String value) {
        return (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("1")) ? true : false;
    }

     public static BufferedImage scaleImage(BufferedImage image, int width, int height) {
        BufferedImage result = image;
        if (width == 1 && height == 1) {
            return result;
        }
        try {
            result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = result.createGraphics();
            AffineTransform at = AffineTransform.getScaleInstance((double) width, (double) height);
            g.drawRenderedImage(image, at);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String cloakId(int id, int seed) {
        String result = "";
        return result;
    }
    private static final char[] HEX_CHARS = {'0', '1', '2', '3',
        '4', '5', '6', '7',
        '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f',};

    /**
     * Turns array of bytes into string representing each byte as
     * unsigned hex number.
     *
     * @param hash Array of bytes to convert to hex-string
     * @return Generated hex string
     */
    public static String asHex(byte hash[]) {
        char buf[] = new char[hash.length * 2];
        for (int i = 0, x = 0; i < hash.length; i++) {
            buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
            buf[x++] = HEX_CHARS[hash[i] & 0xf];
        }
        return new String(buf);
    }

    public static String cloakUrl(String url) throws Exception {
        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
        digest.update(url.getBytes());
        return asHex(digest.digest());
    }
}
