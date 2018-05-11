package com.theonlylies.musictagger.utils.edit;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.theonlylies.musictagger.utils.PreferencesManager;
import com.theonlylies.musictagger.utils.adapters.MusicFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linuxoid on 23.12.17.
 */

public class MediaStoreUtils {

    private static final String media_provider_path = "/Android/data/com.android.providers.media/albumthumbs/";

    static public MusicFile getMusicFileByPath(@NonNull String path, Context context) {
        Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        path = "\"" + path + "\"";
        Cursor cursor = context.getContentResolver().query(contentUri, null, MediaStore.Audio.Media.IS_MUSIC + "!= 0 and " + MediaStore.Audio.Media.DATA + "==" + path, null, null);
        String s;
        MusicFile musicFile = new MusicFile();
        if (cursor.moveToFirst()) {
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
            s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER));
            musicFile.setComposer(s);

            musicFile.setArtworkUri(getAlbumArtUriByAlbumId(id));


        }
        return musicFile;
    }

    static private Uri getAlbumArtUriByAlbumId(long id) {
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(sArtworkUri, id);
    }

    static public void scanFile(String filePath, Context context, MediaScannerConnection.OnScanCompletedListener listener) {
        MediaScannerConnection.scanFile(context, new String[]{filePath}, null, listener);
    }

    static public void scanFiles(String[] paths, Context context, MediaScannerConnection.OnScanCompletedListener listener) {
        MediaScannerConnection.scanFile(context, paths, null, listener);
    }


    static public void updateFileMediaStoreMedia(MusicFile musicFile, Context context, MediaScannerConnection.OnScanCompletedListener listener) {
        scanFile(musicFile.getRealPath(), context, listener);
    }

    static public String generateNewName(MusicFile musicFile, PreferencesManager.RenameRules rule) {
        String newName;
        switch (rule) {
            case title:
                newName = musicFile.getTitle() + ".mp3";
                break;
            case title_album:
                newName = musicFile.getTitle() + "-" + musicFile.getAlbum() + ".mp3";
                break;
            case title_artist:
                newName = musicFile.getTitle() + "-" + musicFile.getArtist() + ".mp3";
                break;
            case title_album_artist:
                newName = musicFile.getTitle() + "-" + musicFile.getAlbum() + "-" + musicFile.getArtist() + ".mp3";
                break;
            default:
                newName = new File(musicFile.getRealPath()).getName();
        }
        return newName;
    }


    static public void updateMuchFilesMediaStore(List<MusicFile> musicFiles, Context context,
                                                 MediaScannerConnection.OnScanCompletedListener listener) {
        ArrayList<String> paths = new ArrayList<>(musicFiles.size());
        for (MusicFile q : musicFiles) {
                paths.add(q.getRealPath());
            }

        scanFiles(paths.toArray(new String[paths.size()]), context, listener);
        }

        static public boolean setAlbumArt (Bitmap bitmap, Context context,long album_id){
            Log.d("SetAlbumArt", "FUNCTION STARTED");
            Uri contentUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
            Cursor cursor = context.getContentResolver().query(contentUri, null, MediaStore.Audio.Albums._ID + "==" + album_id, null, null);
            String albumart_path;
            if (cursor != null && cursor.moveToFirst()) {
                albumart_path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                /////////
                if (albumart_path != null) {
                    try {
                        File file = new File(albumart_path);
                        FileOutputStream output = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
                        output.flush();
                        output.close();
                        Log.d("SetAlbumArt", "file rewrited " + albumart_path);
                        Log.d("bitmap is null", bitmap == null ? "true" : "false");
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                } else {
                    if (new File(Environment.getExternalStorageDirectory().getPath() + media_provider_path).exists()) {
                        Log.d("file exists", "qwerty");
                        try {
                            File file = new File(Environment.getExternalStorageDirectory().getPath() + media_provider_path + "/" + System.currentTimeMillis());
                            if (file.createNewFile()) {
                                FileOutputStream output = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
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
                    } else return false;
                }
            } else return false;

            return true;
        }

        static public boolean deleteAlbumArt (Context context,long album_id){
            Uri contentUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
            Cursor cursor = context.getContentResolver().query(contentUri, null, MediaStore.Audio.Albums._ID + "==" + album_id, null, null);
            String albumart_path;
            if (cursor != null && cursor.moveToFirst()) {
                albumart_path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                /////////
                if (albumart_path != null) {
                    File file = new File(albumart_path);
                    return file.delete();
                }
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                int deleted = context.getContentResolver().delete(ContentUris.withAppendedId(sArtworkUri, album_id), null, null);
            }
            return false;
        }

        public static String[] GENRES = {"Acapella", "Acid", "Acid Jazz", "Acid Punk", "Acoustic", "AlternRock",
                "Alternative", "Ambient", "Anime", "Avantgarde", "Ballad", "Bass", "Beat", "Bebob", "Big Band",
                "Black Metal", "Bluegrass", "Blues", "Booty Bass", "BritPop", "Cabaret", "Celtic", "Chamber Music",
                "Chanson", "Chorus", "Christian Gangsta Rap", "Christian Rap", "Christian Rock", "Classic Rock", "Classical",
                "Club", "Club-House", "Comedy", "Contemporary Christian", "Country", "Cover", "Crossover", "Cult", "Dance",
                "Dance Hall", "Darkwave", "Death Metal", "Disco", "Dream", "Drum & Bass", "Drum Solo", "Duet", "Easy Listening",
                "Electronic", "Ethnic", "Euro-House", "Euro-Techno", "Eurodance", "Fast Fusion", "Folk", "Folk-Rock", "Folklore",
                "Freestyle", "Funk", "Fusion", "Game", "Gangsta", "Goa", "Gospel", "Gothic", "Gothic Rock", "Grunge", "Hard Rock",
                "Hardcore", "Heavy Metal", "Hip-Hop", "House", "Humour", "Indie", "Industrial", "Instrumental", "Instrumental Pop",
                "Instrumental Rock", "JPop", "Jazz", "Jazz+Funk", "Jungle", "Latin", "Lo-Fi", "Meditative", "Merengue", "Metal",
                "Musical", "National Folk", "Native American", "Negerpunk", "New Age", "New Wave", "Noise", "Oldies", "Opera", "Other",
                "Polka", "Polsk Punk", "Pop", "Pop-Folk", "Pop/Funk", "Porn Groove", "Power Ballad", "Pranks", "Primus", "Progressive Rock",
                "Psychadelic", "Psychedelic Rock", "Punk", "Punk Rock", "R&B", "Rap", "Rave", "Reggae", "Remix", "Retro", "Revival",
                "Rhythmic Soul", "Rock", "Rock & Roll", "Salsa", "Samba", "Satire", "Showtunes", "Ska", "Slow Jam", "Slow Rock", "Sonata",
                "Soul", "Sound Clip", "Soundtrack", "Southern Rock", "Space", "Speech", "Swing", "Symphonic Rock", "Symphony", "SynthPop",
                "Tango", "Techno", "Techno-Industrial", "Terror", "Thrash Metal", "Top 40", "Trailer", "Trance", "Tribal", "Trip-Hop", "Vocal"};

        static public void dumpMedia (Context context){
            Log.d("START DUMP MEDIA", "START DUMP MEDIA");
            final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            final String[] cursor_cols = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.ARTIST_ID,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DATA};
            final String where = MediaStore.Audio.Media.IS_MUSIC + "!=0";
            final Cursor cursor = context.getContentResolver().query(uri, cursor_cols, where, null, null);
            if (cursor != null) {
                try {
                    while (cursor.moveToNext()) {

                        String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                        Log.d("ARTIST", artist = artist != null ? artist : "____EMPTY____");
                        String artistId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
                        Log.d("ARTIST ID", artistId = artistId != null ? artistId : "____EMPTY____");
                        String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                        Log.d("ALBUM", album = album != null ? album : "____EMPTY____");
                        String albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                        Log.d("ALBUM ID", albumId = albumId != null ? albumId : "____EMPTY____");
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                        Log.d("TITLE", title = title != null ? title : "____EMPTY____");
                        String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        Log.d("DATA", data = data != null ? data : "____EMPTY____");
                        String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                        Log.d("ID", id = id != null ? id : "____EMPTY____");
                        Log.d("END", "________________________________________________________________________________");
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }
            }
            Log.d("END DUMP MEDIA", "END DUMP MEDIA");
        }

        static public void dumpAlbums (Context context){
            Log.d("START DUMP ALBUMS", "START DUMP ALBUMS");
            final Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
            final String[] cursor_cols = {
                    MediaStore.Audio.Albums._ID,
                    MediaStore.Audio.Albums.ARTIST,
                    MediaStore.Audio.Albums.ALBUM,
                    MediaStore.Audio.Albums.ALBUM_ID,
                    MediaStore.Audio.Albums.ALBUM_ART};
            final Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    while (cursor.moveToNext()) {

                        String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST));
                        Log.d("ARTIST", artist = artist != null ? artist : "____EMPTY____");
                        String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));
                        Log.d("ALBUM", album = album != null ? album : "____EMPTY____");
                        String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID));
                        Log.d("ALBUM ID", id = id != null ? id : "____EMPTY____");
                        String albumArt = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                        Log.d("ALBUM ART", albumArt = albumArt != null ? albumArt : "____EMPTY____");
                        Log.d("END", "________________________________________________________________________________");
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }
            }

            Log.d("END DUMP ALBUMS", "END DUMP ALBUMS");
        }
    }