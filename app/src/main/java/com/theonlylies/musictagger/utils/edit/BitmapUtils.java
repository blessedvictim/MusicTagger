package com.theonlylies.musictagger.utils.edit;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

/**
 * Created by theonlylies on 06.01.18.
 */

public class BitmapUtils {
    @Nullable
    public static Bitmap getCenterCropedBitmap(Bitmap source) {
        if (source == null) return null;

        Bitmap dstBmp;
        if (source.getWidth() >= source.getHeight()) {

            dstBmp = Bitmap.createBitmap(
                    source,
                    source.getWidth() / 2 - source.getHeight() / 2,
                    0,
                    source.getHeight(),
                    source.getHeight()
            );

        } else {

            dstBmp = Bitmap.createBitmap(
                    source,
                    0,
                    source.getHeight() / 2 - source.getWidth() / 2,
                    source.getWidth(),
                    source.getWidth()
            );
        }
        return dstBmp;
    }

    /**
     * @return sized to 800*800 bitmap
     */
    @Nullable
    public static Bitmap getResizedBitmap(Bitmap source) {
        if (source == null) return null;
        if (source.getWidth() > 800 && source.getHeight() > 800) {
            return Bitmap.createScaledBitmap(source, 800, 800, false);
        } else return source;
    }
}
