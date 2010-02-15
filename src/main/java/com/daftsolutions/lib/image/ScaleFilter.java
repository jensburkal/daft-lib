/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daftsolutions.lib.image;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;

/**
 *
 * @author colin
 */
public class ScaleFilter implements BufferedImageOp {

    /**
     * Scales an image using the area-averaging algorithm, which can't be done with AffineTransformOp.
     */
    private int width;
    private int height;

    /**
     * Construct a ScaleFilter.
     */
    public ScaleFilter() {
        this(32, 32);
    }

    /**
     * Construct a ScaleFilter.
     * @param width the width to scale to
     * @param height the height to scale to
     */
    public ScaleFilter(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        if (dst == null) {
            ColorModel dstCM = src.getColorModel();
            dst = new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(width, height), dstCM.isAlphaPremultiplied(), null);
        }

        Image scaleImage = src.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
        Graphics2D g = dst.createGraphics();
        g.drawImage(scaleImage, 0, 0, width, height, null);
        g.dispose();

        return dst;
    }

    public String toString() {
        return "Distort/Scale";
    }

    public Rectangle2D getBounds2D(BufferedImage src) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public RenderingHints getRenderingHints() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
