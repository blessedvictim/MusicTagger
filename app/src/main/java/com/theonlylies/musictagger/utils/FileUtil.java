package com.theonlylies.musictagger.utils;

/**
 * Created by theonlylies on 11.01.18.
 */


import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for helping parsing file systems.
 */
public abstract class FileUtil {

    static public boolean canWriteThisFileSAF(Context context, String path) {
        String access = PreferencesManager.getStringValue(context, "sdcard_uri", null);

        if (access != null) {
            //TODO create write check for prove this preference
            try {
                DocumentFile file1 = getDocumentFile(new File(path), false, context);
                Log.d("canWriteThisFileSAF", String.valueOf(file1.canWrite()));
                //DocumentFile file = DocumentFile.fromTreeUri(context, Uri.parse(access));
                if (file1 != null && file1.canWrite()) return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    static public boolean haveSdCardWriteAccess(Context context) {
        String access = PreferencesManager.getStringValue(context, "sdcard_uri",null);

        if (access != null) {
            //TODO create write check for prove this preference
            try {
                DocumentFile file = DocumentFile.fromTreeUri(context, Uri.parse(access));
                if (file.canWrite()) return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static boolean hasRomovableDeivce(Context context) {
        return getRemovableDevicePath(context) != null;
    }

    private static String getRemovableDevicePath(Context context) {
        File[] files = context.getExternalFilesDirs(null);
        File extFile = Environment.getExternalStorageDirectory();
        if (files.length > 1) {
            for (File f : files) {
                if (f.getPath().startsWith(extFile.getPath())) continue;
                else return f.getPath();
            }
        }
        return null;
    }


    static public boolean fileOnSdCard(File file) {
        /*StorageHelper storageHelper = StorageHelper.getInstance();
        for (StorageHelper.MountDevice mountDevice : storageHelper.getAllMountedDevices()) {
            Log.d("EXTSDCARD", mountDevice.getPath());
            Log.d("file path",file.getPath());
            if (mountDevice.getType() == StorageHelper.MountDeviceType.REMOVABLE_SD_CARD) {

                if (file.getAbsolutePath().startsWith(mountDevice.getPath())) {
                    return true;
                }

            }
        }
        return false;*/
        return !file.getPath().startsWith(Environment.getExternalStorageDirectory().getPath());
    }
    /**
     * Determine the camera folder. There seems to be no Android API to work for real devices, so this is a best guess.
     *
     * @return the default camera folder.
     */

    /**
     * Copy a file. The target file may even be on external SD card for Kitkat.
     *
     * @param source The source file
     * @param target The target file
     * @return true if the copying was successful.
     */
    @SuppressWarnings("null")
    public static boolean copyFile(final File source, final File target, Context context) {
        FileInputStream inStream = null;
        OutputStream outStream = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inStream = new FileInputStream(source);

            // First try the normal way
            if (isWritable(target)) {
                // standard way
                outStream = new FileOutputStream(target);
                inChannel = inStream.getChannel();
                outChannel = ((FileOutputStream) outStream).getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Storage Access Framework
                    DocumentFile targetDocument = getDocumentFile(target, false, context);
                    outStream =
                            context.getContentResolver().openOutputStream(targetDocument.getUri());
                } else {
                    return false;
                }

                if (outStream != null) {
                    // Both for SAF and for Kitkat, write to output stream.
                    byte[] buffer = new byte[16384]; // MAGIC_NUMBER
                    int bytesRead;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }

            }
        } catch (Exception e) {
            Log.e("AmazeFileUtils",
                    "Error when copying file from " + source.getAbsolutePath() + " to " + target.getAbsolutePath(), e);
            return false;
        } finally {
            try {
                inStream.close();
            } catch (Exception e) {
                // ignore exception
            }
            try {
                outStream.close();
            } catch (Exception e) {
                // ignore exception
            }
            try {
                inChannel.close();
            } catch (Exception e) {
                // ignore exception
            }
            try {
                outChannel.close();
            } catch (Exception e) {
                // ignore exception
            }
        }
        return true;
    }

    public static OutputStream getOutputStream(@NonNull final File target, Context context, long s) throws Exception {

        OutputStream outStream = null;
        try {
            // First try the normal way
            if (isWritable(target)) {
                // standard way
                outStream = new FileOutputStream(target);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Storage Access Framework
                    DocumentFile targetDocument = getDocumentFile(target, false, context);
                    outStream =
                            context.getContentResolver().openOutputStream(targetDocument.getUri());
                }


            }
        } catch (Exception e) {
            Log.e("AmazeFileUtils",
                    "Error when copying file from " + target.getAbsolutePath(), e);
        }
        return outStream;
    }

    /**
     * Delete a file. May be even on external SD card.
     *
     * @param file the file to be deleted.
     * @return True if successfully deleted.
     */
    public static final boolean deleteFile(@NonNull final File file, Context context) {
        // First try the normal deletion.
        if (file == null) return true;
        boolean fileDelete = deleteFilesInFolder(file, context);
        if (file.delete() || fileDelete)
            return true;

        // Try with Storage Access Framework.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && FileUtil.isOnExtSdCard(file, context)) {

            DocumentFile document = getDocumentFile(file, false, context);
            return document.delete();
        }


        return !file.exists();
    }

    /**
     * Get a temp file.
     *
     * @param file The base file for which to create a temp file.
     * @return The temp file.
     */
    public static final File getTempFile(@NonNull final File file, Context context) {
        File extDir = context.getExternalFilesDir(null);
        File tempFile = new File(extDir, file.getName());
        return tempFile;
    }

    /**
     * Delete a folder.
     *
     * @param file The folder name.
     * @return true if successful.
     */
    public static boolean rmdir1(final File file, Context context) {
        if (file == null)
            return false;
        boolean b = true;
        for (File file1 : file.listFiles()) {
            if (file1.isDirectory()) {
                if (!rmdir1(file1, context)) b = false;
            } else {
                if (!deleteFile(file1, context)) b = false;
            }
        }
        return b;
    }

    public static boolean rmdir(final File file, Context context) {
        if (file == null)
            return false;
        if (!file.exists()) {
            return true;
        }
        if (!file.isDirectory()) {
            return false;
        }
        String[] fileList = file.list();
        if (fileList != null && fileList.length > 0) {
            //  empty the folder.
            rmdir1(file, context);
        }
        String[] fileList1 = file.list();
        if (fileList1 != null && fileList1.length > 0) {
            // Delete only empty folder.
            return false;
        }
        // Try the normal way
        if (file.delete()) {
            return true;
        }

        // Try with Storage Access Framework.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DocumentFile document = getDocumentFile(file, true, context);
            return document.delete();
        }

        // Try the Kitkat workaround.
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            // Delete the created entry, such that content provider will delete the file.
            resolver.delete(MediaStore.Files.getContentUri("external"), MediaStore.MediaColumns.DATA + "=?",
                    new String[]{file.getAbsolutePath()});
        }

        return !file.exists();
    }

    /**
     * Delete all files in a folder.
     *
     * @param folder the folder
     * @return true if successful.
     */
    public static final boolean deleteFilesInFolder(final File folder, Context context) {
        boolean totalSuccess = true;
        if (folder == null)
            return false;
        if (folder.isDirectory()) {
            for (File child : folder.listFiles()) {
                deleteFilesInFolder(child, context);
            }

            if (!folder.delete())
                totalSuccess = false;
        } else {

            if (!folder.delete())
                totalSuccess = false;
        }
        return totalSuccess;
    }

    /**
     * Check is a file is writable. Detects write issues on external SD card.
     *
     * @param file The file
     * @return true if the file is writable.
     */
    public static final boolean isWritable(final File file) {
        if (file == null)
            return false;
        boolean isExisting = file.exists();

        try {
            FileOutputStream output = new FileOutputStream(file, true);
            try {
                output.close();
            } catch (IOException e) {
                // do nothing.
            }
        } catch (FileNotFoundException e) {
            return false;
        }
        boolean result = file.canWrite();

        // Ensure that file is not created during this process.
        if (!isExisting) {
            file.delete();
        }

        return result;
    }

    // Utility methods for Android 5

    /**
     * Check for a directory if it is possible to create files within this directory, either via normal writing or via
     * Storage Access Framework.
     *
     * @param folder The directory
     * @return true if it is possible to write in this directory.
     */
    public static final boolean isWritableNormalOrSaf(final File folder, Context c) {
        // Verify that this is a directory.
        if (folder == null)
            return false;
        if (!folder.exists() || !folder.isDirectory()) {
            return false;
        }

        // Find a non-existing file in this directory.
        int i = 0;
        File file;
        do {
            String fileName = "AugendiagnoseDummyFile" + (++i);
            file = new File(folder, fileName);
        }
        while (file.exists());

        // First check regular writability
        if (isWritable(file)) {
            return true;
        }

        // Next check SAF writability.
        DocumentFile document = getDocumentFile(file, false, c);

        if (document == null) {
            return false;
        }

        // This should have created the file - otherwise something is wrong with access URL.
        boolean result = document.canWrite() && file.exists();

        // Ensure that the dummy file is not remaining.
        deleteFile(file, c);
        return result;
    }

    /**
     * Get a list of external SD card paths. (Kitkat or higher.)
     *
     * @return A list of external SD card paths.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPaths(Context context) {
        List<String> paths = new ArrayList<String>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("AmazeFileUtils", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        if (paths.isEmpty()) paths.add("/storage/sdcard1");
        return paths.toArray(new String[0]);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPathsForActivity(Context context) {
        List<String> paths = new ArrayList<String>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("AmazeFileUtils", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        if (paths.isEmpty()) paths.add("/storage/sdcard1");
        return paths.toArray(new String[0]);
    }

    /**
     * Determine the main folder of the external SD card containing the given file.
     *
     * @param file the file.
     * @return The main folder of the external SD card containing this file, if the file is on an SD card. Otherwise,
     * null is returned.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getExtSdCardFolder(final File file, Context context) {
        String[] extSdPaths = getExtSdCardPaths(context);
        try {
            for (int i = 0; i < extSdPaths.length; i++) {
                if (file.getCanonicalPath().startsWith(extSdPaths[i])) {
                    return extSdPaths[i];
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * Determine if a file is on external sd card. (Kitkat or higher.)
     *
     * @param file The file.
     * @return true if on external sd card.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isOnExtSdCard(final File file, Context c) {
        return getExtSdCardFolder(file, c) != null;
    }

    /**
     * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If the file is not
     * existing, it is created.
     *
     * @param file        The file.
     * @param isDirectory flag indicating if the file should be a directory.
     * @return The DocumentFile
     */
    public static DocumentFile getDocumentFile(final File file, final boolean isDirectory, Context context) {
        String baseFolder = getExtSdCardFolder(file, context);
        Log.d("baseFolder", baseFolder);
        boolean originalDirectory = false;
        if (baseFolder == null) {
            return null;
        }

        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            if (!baseFolder.equals(fullPath))
                relativePath = fullPath.substring(baseFolder.length() + 1);
            else originalDirectory = true;
        } catch (IOException e) {
            return null;
        } catch (Exception f) {
            originalDirectory = true;
            //continue
        }
        String as = PreferencesManager.getStringValue(context, "sdcard_uri",null);
        //TO DO windows with write access

        Uri treeUri = null;
        if (as != null) treeUri = Uri.parse(as);
        if (treeUri == null) {
            return null;
        }

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
        if (originalDirectory) return document;
        String[] parts = relativePath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            //my implementation
            if (document != null) {
                document = document.findFile(parts[i]);
            }


            /*if (nextDocument == null) {
                if ((i < parts.length - 1) || isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                } else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }*/ //ORIGINAL CODE
            // document = nextDocument;
        }

        return document;
    }

    // Utility methods for Kitkat

    /**
     * Copy a resource file into a private target directory, if the target does not yet exist. Required for the Kitkat
     * workaround.
     *
     * @param resource   The resource file.
     * @param folderName The folder below app folder where the file is copied to.
     * @param targetName The name of the target file.
     * @return the dummy file.
     * @throws IOException
     */
    private static File copyDummyFile(final int resource, final String folderName, final String targetName, Context context)
            throws IOException {
        File externalFilesDir = context.getExternalFilesDir(folderName);
        if (externalFilesDir == null) {
            return null;
        }
        File targetFile = new File(externalFilesDir, targetName);

        if (!targetFile.exists()) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = context.getResources().openRawResource(resource);
                out = new FileOutputStream(targetFile);
                byte[] buffer = new byte[4096]; // MAGIC_NUMBER
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        // do nothing
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        // do nothing
                    }
                }
            }
        }
        return targetFile;
    }

    /**
     * Copy the dummy image and dummy mp3 into the private folder, if not yet there. Required for the Kitkat workaround.
     *
     * @return the dummy mp3.
     * <p/>
     * private static File copyDummyFiles(Context c) {
     * try {
     * copyDummyFile(R.mipmap.ic_launcher, "mkdirFiles", "albumart.jpg",c);
     * return copyDummyFile(R.raw.temptrack, "mkdirFiles", "temptrack.mp3",c);
     * <p/>
     * }
     * catch (IOException e) {
     * Log.e("AmazeFileUtils", "Could not copy dummy files.", e);
     * return null;
     * }
     * }
     */
    static class MediaFile {

        private static final String NO_MEDIA = ".nomedia";
        private static final String ALBUM_ART_URI = "content://media/external/audio/albumart";
        private static final String[] ALBUM_PROJECTION = {BaseColumns._ID, MediaStore.Audio.AlbumColumns.ALBUM_ID, "media_type"};

        private static File getExternalFilesDir(Context context) {


            try {
                Method method = Context.class.getMethod("getExternalFilesDir", String.class);
                return (File) method.invoke(context, (String) null);
            } catch (SecurityException | IllegalArgumentException ex) {
                //   Log.d(Maui.LOG_TAG, "Unexpected reflection error.", ex);
                return null;
            } catch (NoSuchMethodException ex) {
                //     Log.d(Maui.LOG_TAG, "Unexpected reflection error.", ex);
                return null;
            } catch (IllegalAccessException ex) {
                //Log.d(Maui.LOG_TAG, "Unexpected reflection error.", ex);
                return null;
            } catch (InvocationTargetException ex) {
                //Log.d(Maui.LOG_TAG, "Unexpected reflection error.", ex);
                return null;
            }
        }


        private final File file;
        private final Context context;
        private final ContentResolver contentResolver;
        Uri filesUri;

        public MediaFile(Context context, File file) {
            this.file = file;
            this.context = context;
            contentResolver = context.getContentResolver();
            filesUri = MediaStore.Files.getContentUri("external");
        }

        /**
         * Deletes the file. Returns true if the file has been successfully deleted or otherwise does not exist. This operation is not
         * recursive.
         */
        public boolean delete()
                throws IOException {

            if (!file.exists()) {
                return true;
            }

            boolean directory = file.isDirectory();
            if (directory) {
                // Verify directory does not contain any files/directories within it.
                String[] files = file.list();
                if (files != null && files.length > 0) {
                    return false;
                }
            }

            String where = MediaStore.MediaColumns.DATA + "=?";
            String[] selectionArgs = new String[]{file.getAbsolutePath()};

            // Delete the entry from the media database. This will actually delete media files (images, audio, and video).
            contentResolver.delete(filesUri, where, selectionArgs);

            if (file.exists()) {
                // If the file is not a media file, create a new entry suggesting that this location is an image, even
                // though it is not.
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                // Delete the created entry, such that content provider will delete the file.
                contentResolver.delete(filesUri, where, selectionArgs);
            }

            return !file.exists();
        }

        public File getFile() {
            return file;
        }

    }
}