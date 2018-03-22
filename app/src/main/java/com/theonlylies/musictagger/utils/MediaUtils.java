package com.theonlylies.musictagger.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.theonlylies.musictagger.utils.adapters.MusicFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by theonlylies on 06.03.18.
 */

public class MediaUtils {
    /**
     * return MusicFile object from MediaStore context with path of music file
     *
     * @param context applcation context
     * @param path    path which can be used to search in mediastore
     * @return MusicFile instance of file or null
     */
    static public MusicFile getMusicFileWithPath(Context context, String path) {
        Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        path = "\"" + path + "\"";
        Cursor cursor = context.getContentResolver().query(contentUri, null, MediaStore.Audio.Media.DATA + "==" + path, null, null);
        String s;
        MusicFile musicFile = new MusicFile();
        if (cursor != null && cursor.moveToFirst()) {
            s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            musicFile.setTitle(s);
            s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
            musicFile.setAlbum(s);
            s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
            long id = Long.decode(s);
            musicFile.setAlbum_id(id);
            s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            musicFile.setArtist(s);
            s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK));
            musicFile.setTrackNumber(s);
            s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            musicFile.setRealPath(s);
            s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR));
            musicFile.setYear(s);

            contentUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
            cursor = context.getContentResolver().query(contentUri, null, MediaStore.Audio.Albums._ID + "==" + id, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                musicFile.setArtworkPath(s);
                cursor.close();
            }
        } else return null;

        return musicFile;
    }

    /**
     * delete row from MediaStore where path=path of music file with deleting album art cached file if possible
     *
     * @param context applcation context
     * @param path    path which can be used to search in mediastore
     * @return true oif delete complete or false if error
     */
    static public boolean deleteMusicFileFromMediaStore(Context context, String path) {
        Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        path = "\"" + path + "\"";
        Cursor cursor = context.getContentResolver().query(contentUri, null, MediaStore.Audio.Media.DATA + "==" + path, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String albId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
            long id = Long.decode(albId);
            //album art deleting if need
            if (getCountOfMusicFileInAlbum(context, id) == 1) {
                cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Albums._ID + "==" + id, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    String albumArtPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                    Log.d("deleteArtMusicFile...", "delete file " + albumArtPath + " " + String.valueOf(deleteFile(context, albumArtPath)));
                    cursor.close();
                }
            }

        }
        int deleted = context.getContentResolver().delete(contentUri, MediaStore.Audio.Media.DATA + "==" + path, null);
        return deleted > 0;
    }

    /**
     * Must delete file from external storage or external removable storage
     *
     * @param context
     * @param path
     * @return
     */
    static public boolean deleteFile(Context context, String path) {
        if (path != null) {
            File file = new File(path);
            return file.delete();
        }
        return false;
    }

    /**
     * Insert music file into MediaStore
     *
     * @param context   application context
     * @param musicFile MusicFile instance with tags which will be inserted in MediaStore db
     * @param path      path of music file
     * @return return true if insert succsessfull
     */
    static public boolean insertMusicFileWithPath(Context context, MusicFile musicFile, String path) {
        Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        try {
            String nPath = "\"" + path + "\"";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.DATA, nPath);
            values.put(MediaStore.Audio.Media.TITLE, musicFile.getTitle());
            values.put(MediaStore.Audio.Media.ALBUM, musicFile.getAlbum());
            //values.put(MediaStore.Audio.AudioColumns.ALBUM_ID,musicFile.getAlbum_id());
            values.put(MediaStore.Audio.Media.ARTIST, musicFile.getArtist());
            values.put(MediaStore.Audio.Media.YEAR, musicFile.getYear());
            values.put(MediaStore.Audio.Media.COMPOSER, musicFile.getArtist());
            values.put(MediaStore.Audio.Media.TRACK, musicFile.getTrackNumber());
            values.put(MediaStore.Audio.Media.IS_MUSIC, 1);
            Uri uri = context.getContentResolver().insert(contentUri, values);
            Log.e("uri", uri.toString());
            return uri != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Function must delete album art cached file for album with given id
     *
     * @param context
     * @param album_id
     * @return result of proceed
     */
    static public boolean deleteAlbumArtFileWithAlbumId(Context context, long album_id) {
        Uri contentUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(contentUri, null, MediaStore.Audio.Albums._ID + "==" + album_id, null, null);
        String albumart_path;
        if (cursor != null && cursor.moveToFirst()) {
            albumart_path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
            /////////
            if (albumart_path != null) {
                //File file = new File(albumart_path);
                cursor.close();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Audio.Albums.ALBUM_ART, "");
                //context.getContentResolver().delete(contentUri, MediaStore.Audio.Albums._ID + "==" + album_id, null);
                return deleteFile(context, albumart_path);
            }
        }
        return false;
    }

    /**
     * count of track in album with given album id
     *
     * @param context
     * @param album_id
     * @return
     */
    static public int getCountOfMusicFileInAlbum(Context context, long album_id) {
        Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(contentUri, null, MediaStore.Audio.Media.ALBUM_ID + "==" + album_id, null, null);
        if (cursor != null) {
            return cursor.getCount();
        }
        return 0;
    }

    /**
     * insert album art in MediaStore cache for album with given id
     *
     * @param context
     * @param album_id
     * @param imageBytes
     * @return
     */
    static public boolean insertAlbumArtWithAlbumId(Context context, long album_id, byte[] imageBytes) {
        final String media_provider_path = "/Android/data/com.android.providers.media/albumthumbs/";
        Uri contentUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(contentUri, null, MediaStore.Audio.Albums._ID + "==" + album_id, null, null);
        String albumart_path;
        if (cursor != null && cursor.moveToFirst()) {
            albumart_path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
            cursor.close();
            /////////
            if (albumart_path != null) {
                try {
                    File file = new File(albumart_path);
                    FileOutputStream output = new FileOutputStream(file);
                    output.write(imageBytes);
                    output.flush();
                    output.close();
                    Log.d("SetAlbumArt", "file rewrited " + albumart_path);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                try {
                    File file = new File(Environment.getExternalStorageDirectory().getPath() + media_provider_path + "/" + System.currentTimeMillis());
                    if (file.createNewFile()) {
                        FileOutputStream output = new FileOutputStream(file);
                        output.write(imageBytes);
                        output.flush();
                        output.close();
                        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                        ContentValues values = new ContentValues();
                        values.put("album_id", album_id);
                        values.put("_data", file.getPath());
                        context.getContentResolver().insert(sArtworkUri, values);
                    } else return false;

                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        } else return false;

        return true;
    }


}
