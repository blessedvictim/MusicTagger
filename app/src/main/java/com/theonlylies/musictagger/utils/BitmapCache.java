package com.theonlylies.musictagger.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class BitmapCache {
    public BitmapCache(Context context) {
        rootDir = context.getCacheDir();
        ctx = context;
    }

    private File rootDir;
    File cachedFile;
    Context ctx;


    private static void copyFileUsingChannel(File source, File dest) throws IOException {
        try (
                FileChannel sourceChannel = new FileInputStream(source).getChannel();
                FileChannel destChannel = new FileOutputStream(dest).getChannel()
        ) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }


    public Uri cacheBitmap(Bitmap bitmap) throws Exception {

        File cachedFile = new File(rootDir.getAbsolutePath() + File.pathSeparator + System.currentTimeMillis());
        if (cachedFile.createNewFile()) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cachedFile));
        }

        return Uri.fromFile(cachedFile);
    }

    public Observable<Uri> reactiveCacheBitmap(Bitmap bitmap) throws Exception {

        File cachedFile = new File(rootDir.getAbsolutePath() + File.pathSeparator + System.currentTimeMillis());
        return Observable.fromCallable(() -> {
            Log.e("rxjava", "observe");
            if (cachedFile.createNewFile()) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cachedFile));
                return Uri.fromFile(cachedFile);
            } else return null;
        }).subscribeOn(Schedulers.newThread());

    }


    public void clearCache() {
        cachedFile.delete();
    }
}
