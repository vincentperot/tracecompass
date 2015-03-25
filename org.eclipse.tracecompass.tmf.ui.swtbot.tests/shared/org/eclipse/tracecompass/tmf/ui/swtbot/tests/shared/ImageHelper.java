/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared;

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;

/**
 * Test helpers, allow looking up the frame buffer and testing what is really
 * displayed
 */
public final class ImageHelper {

    private final int[] fPixels;
    private final Rectangle fBounds;

    /**
     * Constructor
     *
     * @param pixels
     *            the pixel map
     * @param bounds
     *            the bounds
     */
    private ImageHelper(int[] pixels, Rectangle bounds) {
        if (pixels.length != bounds.height * bounds.width) {
            throw new IllegalArgumentException("Incoherent image");
        }
        fPixels = Arrays.copyOf(pixels, pixels.length);
        fBounds = bounds;
    }

    /**
     * Gets a screen grab of the rectangle r; the way to access a given pixel is
     * <code>pixel = rect.width*y + x</code>
     *
     * @param rect
     *            the area to grab display relative
     * @return an ImageHelper, cannot be null
     */
    public static ImageHelper getScreenGrab(final Rectangle rect) {
        return UIThreadRunnable.syncExec(new Result<ImageHelper>() {
            @Override
            public ImageHelper run() {
                try {
                    java.awt.Robot rb = new java.awt.Robot();
                    java.awt.image.BufferedImage bi = rb.createScreenCapture(new java.awt.Rectangle(rect.x, rect.y, rect.width, rect.height));
                    return new ImageHelper(bi.getRGB(0, 0, rect.width, rect.height, null, 0, rect.width), rect);
                } catch (AWTException e) {
                }
                return new ImageHelper(new int[0], new Rectangle(0, 0, 0, 0));
            }
        });
    }

    /**
     * Get the bounds
     *
     * @return the bounds
     */
    public Rectangle getBounds() {
        return fBounds;
    }

    /**
     * Get the pixel for a given set of coordinates
     *
     * @param x
     *            x
     * @param y
     *            y
     * @return the RGB, can return an {@link ArrayIndexOutOfBoundsException}
     */
    public RGB getPixel(int x, int y) {
        return getRgbFromRGBPixel(fPixels[x + y * fBounds.width]);
    }

    /**
     * Sample an image at n points
     *
     * @param samplePoints
     *            a list of points to sample at
     * @return a list of RGBs corresponding to the pixel coordinates. Can throw
     *         an {@link IllegalArgumentException} if the pixels are not in the
     *         image or if the image does not match the bounds
     */
    public List<RGB> sample(List<Point> samplePoints) {
        for (Point p : samplePoints) {
            if (!getBounds().contains(p)) {
                throw new IllegalArgumentException("Pixel outside of picture");
            }

        }
        List<RGB> retVal = new ArrayList<>(samplePoints.size());
        for (Point p : samplePoints) {
            retVal.add(getPixel(p.x, p.y));
        }
        return retVal;
    }

    /**
     * Get the color histogram of the image
     *
     * @return The color density of the image
     */
    public Map<RGB, Integer> getHistogram() {
        Map<RGB, Integer> colors = new HashMap<>();
        for (int pixel : fPixels) {
            RGB pixelColor = getRgbFromRGBPixel(pixel);
            Integer pixelCount = colors.get(pixelColor);
            Integer val = pixelCount == null ? 1 : Integer.valueOf(pixelCount.intValue() + 1);
            colors.put(pixelColor, val);
        }
        return colors;
    }

    /**
     * Get the color histogram of the row of the image
     *
     * @param row
     *            the row to lookup
     *
     * @return The x oriented line
     */
    public List<RGB> getPixelRow(int row) {
        List<RGB> retVal = new ArrayList<>();
        for (int x = 0; x < getBounds().width; x++) {
            retVal.add(getPixel(x, row));
        }
        return retVal;
    }

    /**
     * Get the color histogram of a column of the image
     *
     * @param col
     *            the column to lookup
     *
     * @return The y oriented line
     */
    public List<RGB> getPixelColumn(int col) {
        List<RGB> retVal = new ArrayList<>();
        for (int y = 0; y < getBounds().height; y++) {
            retVal.add(getPixel(col, y));
        }
        return retVal;
    }

    /**
     * Difference between two images (this - other)
     *
     * @param other
     *            the other image to compare
     * @return an image that is the per pixel difference between the two images
     */
    public ImageHelper diff(ImageHelper other) {
        if (other.getBounds().width != fBounds.width && other.getBounds().height != fBounds.height) {
            throw new IllegalArgumentException("Different sized images");
        }
        int[] fBuffer = new int[fPixels.length];
        for (int i = 0; i < fPixels.length; i++) {
            RGB local = getRgbFromRGBPixel(fPixels[i]);
            RGB otherPixel = getRgbFromRGBPixel(other.fPixels[i]);
            byte r = (byte) (local.red - otherPixel.red);
            byte g = (byte) (local.green - otherPixel.green);
            byte b = (byte) (local.blue - otherPixel.blue);
            fBuffer[i] = r << 16 + g << 8 + b;
        }
        return new ImageHelper(fBuffer, getBounds());
    }

    /**
     * Write the image to disk in PNG form
     *
     * @param outputFile
     *            the file to write it to
     * @throws IOException
     *             filenotfound and such
     */
    public void writePng(File outputFile) throws IOException {
        java.awt.image.BufferedImage theImage = new java.awt.image.BufferedImage(fBounds.width, fBounds.height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        theImage.setRGB(0, 0, fBounds.width, fBounds.height, fPixels, 0, fBounds.width);
        ImageIO.write(theImage, "png", outputFile);
    }

    private static RGB getRgbFromRGBPixel(int pixel) {
        return new RGB(((pixel >> 16) & 0xff), ((pixel >> 8) & 0xff), ((pixel) & 0xff));
    }
}
