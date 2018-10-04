package com.coyoapp.crap.android.test.craptest;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.Size;

public class ImageConverter {

    private static float[] hsv = new float[3];

    public static Bitmap toBwr(Bitmap source) {
        @ColorInt int[] row = new int[source.getWidth()];
        Bitmap result = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < source.getHeight(); y++) {
            source.getPixels(row, 0, source.getWidth(), 0, y, source.getWidth(), 1);
            for (int x = 0; x < source.getWidth(); x++) {
                row[x] = toBwr(row[x]);
            }
            result.setPixels(row, 0, source.getWidth(), 0, y, source.getWidth(), 1);
        }
        return result;
    }

    public static byte[] uncompressed(Bitmap bitmap) {
        int x = 200;
        int y = 200;
        int w = Math.min(x, bitmap.getWidth());
        int h = Math.min(y, bitmap.getHeight());
        int sizePerColor = ((w + 7) / 8) * h;
        byte[] black = new byte[sizePerColor];
        byte[] red = new byte[sizePerColor];
        @ColorInt int[] px = new int[8];
        int pos = 0;
        for (int cy = 0; cy < h; cy++) {
            for (int cx = 0; cx < w; cx += 8, pos++) {
                bitmap.getPixels(px, 0, w, cx, cy, 8, 1);
                black[pos] = getByteFrom8Pixels(px, Color.BLACK);
                red[pos] = getByteFrom8Pixels(px, Color.RED);
            }
        }
        byte[] buffer = new byte[5 + 2 * sizePerColor];
        buffer[0] = 0; // format
        buffer[1] = 0; // x / 8
        buffer[2] = 0; // y / 8
        buffer[3] = (byte) w;
        buffer[4] = (byte) h;
        System.arraycopy(black, 0, buffer, 5, sizePerColor);
        System.arraycopy(red, 0, buffer, 5 + sizePerColor, sizePerColor);
        return buffer;
    }

    private static byte getByteFrom8Pixels(@ColorInt @Size(8) int[] px, @ColorInt int cmpColor) {
        int v = 0;
        for (int pxp = 0; pxp < 7; pxp++) {
            v = v << 1;
            v = v | (px[pxp] == cmpColor ? 1 : 0);
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
