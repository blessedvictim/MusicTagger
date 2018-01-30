package com.theonlylies.musictagger.utils.edit;

/**
 * Created by theonlylies on 30.01.18.
 */

import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.theonlylies.musictagger.utils.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;

/**
 * Created by Alex on 04.07.2016.
 */
public class MusicFileReplacer {

    public MusicFileReplacer(Context context) {
        rootDir = context.getCacheDir();
        Log.d("CACHE:", context.getCacheDir().getAbsolutePath());
        ctx = context;
    }


    private File rootDir;
    File cachedFile;
    File src;
    Context ctx;

    static public boolean isWriteble(File file) {
        DocumentFile doc = DocumentFile.fromFile(file);
        return doc.canWrite();
    }

    public File cacheMusicFile(File src) throws Exception {
        FileInputStream inStream = null;
        File dst = new File(rootDir.getAbsolutePath() + "/" + src.getName());
        this.src = src;
        dst.createNewFile();
        inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inStream.read(buffer)) > 0) {
            outStream.write(buffer, 0, length);
        }

        inStream.close();
        outStream.close();
        Log.d("cachedPath:", dst.getAbsolutePath());
        Log.d("sourcePATH:", src.getAbsolutePath());
        cachedFile = dst;
        return cachedFile;
    }

    public void replaceCache() {
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(cachedFile);
            OutputStream outputStream = FileUtil.getOutputStream(src, ctx, 0);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inStream.close();
            Log.d("cached file deleted ? ", Boolean.toString(deleteCachedFile()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deleteCachedFile() {
        return cachedFile.delete();
    }
}