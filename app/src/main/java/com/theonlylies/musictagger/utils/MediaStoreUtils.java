package com.theonlylies.musictagger.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by linuxoid on 23.12.17.
 */

public class MediaStoreUtils  {
    private Context context;
    public ArrayList<HashMap<String,String> > list;



    public MediaStoreUtils(Context context){
        this.context = context;
        list = new ArrayList<>();
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj,
                null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        if(cursor.moveToFirst()){
            String a = cursor.getString(column_index);
            cursor.close();
            return a;
        }
        return null;
    }

    public boolean hasArtPathInMediaStore(String path){
        Uri content = getArtUriFromPath(path);
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(content, proj,
                null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        if(cursor.moveToFirst()){
            if(cursor.getString(column_index)==null){
                return false;
            }
            cursor.close();
            return true;
        }
        return false;
    }

    public String getAlbumIdFromPath(String path){
        for(int i=0;i<list.size();i++){
            if(list.get(i).get(MediaStore.Audio.Media.DATA).equals(path) ){
                String albumId = list.get(i).get(MediaStore.Audio.Media.ALBUM_ID);

                return albumId;
            }else{
                continue;
            }
        }
        return null;
    }

    public void addArtworkToMediaStore(String artPath,String filePath){
        Uri uri;
        uri = Uri.parse("content://media/external/audio/albumart");
        ContentValues values = new ContentValues();
        //values.put("album_id", getAlbumIdFromPath(path));
        values.put("_data", artPath);
        //Log.d("LEL",getAlbumIdFromPath(filePath));
        context.getContentResolver().update(ContentUris.withAppendedId(uri, Integer.decode(getAlbumIdFromPath(filePath))), values, null, null);

    }

    public void deleteArtworkFromMediaStore(String filePath){
        Uri uri;
        uri = Uri.parse("content://media/external/audio/albumart");
        context.getContentResolver().delete(ContentUris.withAppendedId(uri, Integer.decode(getAlbumIdFromPath(filePath))),null, null);
    }

    public Uri getArtUriFromPath(String path){
        Log.d("AADASDSAFSAFSFSFSFAFA",path);
        for(int i=0;i<list.size();i++){
            if(list.get(i).get(MediaStore.Audio.Media.DATA).equals(path) ){
                String albumId = list.get(i).get(MediaStore.Audio.Media.ALBUM_ID);
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, Long.decode(albumId));
                return albumArtUri;
            }
        }
        return null;
    }

}