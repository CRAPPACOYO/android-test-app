package com.coyoapp.crap.android.test.craptest;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.Size;

public class ImageConverter {

    private static float[] hsv = new float[3];

    public static Bitmap toBwr(Bitmap source) {
        @ColorInt int[] row = new int[source.getWidth()];
        Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        for (int y = 0; y < source.getHeight(); y++) {
            source.getPixels(row, 0, source.getWidth(), 0, y, source.getWidth(), 1);
            for (int x = 0; x < source.getWidth(); x++) {
                row[x] = toBwr(row[x]);
            }
            result.setPixels(row, 0, source.getWidth(), 0, y, source.getWidth(), 1);
        }
        return result;
    }

    public static byte[] uncompressed(Bitmap bitmap, Rect area) {
        int width = Math.min(area.width(), bitmap.getWidth());
        int height = Math.min(area.height(), bitmap.getHeight());
        area = new Rect(area.left, area.top, width, height);
        int bitmapSizePerColor = ((width + 7) / 8) * height;
        byte[] black = new byte[bitmapSizePerColor];
        byte[] red = new byte[bitmapSizePerColor];
        @ColorInt int[] px = new int[8];
        int pos = 0;
        for (int cy = 0; cy < height; cy++) {
            for (int cx = 0; cx < width; cx += 8, pos++) {
                bitmap.getPixels(px, 0, width, cx, cy, 8, 1);
                black[pos] = getByteFrom8Pixels(px, Color.BLACK);
                red[pos] = getByteFrom8Pixels(px, Color.RED);
            }
        }
        byte[] coordinates;
        boolean isShortCoordinates = isShort(area);
        coordinates = isShortCoordinates ? getShortCoordinatesHeader(area) : getLongCoordinatesHeader(area);
        byte[] buffer = new byte[1 + coordinates.length + (2 * bitmapSizePerColor)];
        buffer[0] = (byte) (isShortCoordinates ? 0 : 1);
        int bpos = 1;
        System.arraycopy(coordinates, 0, buffer, bpos, coordinates.length);
        bpos += coordinates.length;
        System.arraycopy(black, 0, buffer, bpos, bitmapSizePerColor);
        bpos += bitmapSizePerColor;
        System.arraycopy(red, 0, buffer, bpos, bitmapSizePerColor);
        return buffer;
    }

    private static byte[] getLongCoordinatesHeader(Rect area) {
        return new byte[]{(byte) (area.left >> 8), (byte) area.left,
                (byte) (area.top >> 8), (byte) area.top,
                (byte) (area.width() >> 8), (byte) area.width(),
                (byte) (area.height() >> 8), (byte) area.height()};
    }

    private static byte[] getShortCoordinatesHeader(Rect area) {
        return new byte[]{(byte) (area.left / 8), (byte) (area.top / 8),
                (byte) (area.width() / 8), (byte) (area.height() / 8)};
    }

    private static boolean isShort(Rect area) {
        return isShort(area.left) && isShort(area.top)
                && isShort(area.width()) && isShort(area.height());
    }

    private static boolean isShort(int value) {
        return (value % 8) == 0;
    }

    private static byte getByteFrom8Pixels(@ColorInt @Size(8) int[] px, @ColorInt int cmpColor) {
        int v = 0;
        for (int pxp = 0; pxp < 7; pxp++) {
            v = (v << 1) | (px[pxp] == cmpColor ? 1 : 0);
        }
        return (byte) v;
    }

    @ColorInt
    private static int toBwr(@ColorInt int color) {
        Color.colorToHSV(color, hsv);
        boolean isRed = ((hsv[0] < 30) || (hsv[0] > 330)) && (hsv[1] > 0.6);
        boolean isBlack = hsv[2] < 0.4;
        return isBlack ? Color.BLACK : (isRed ? Color.RED : Color.WHITE);
    }
}
