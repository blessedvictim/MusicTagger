package com.theonlylies.musictagger.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by theonlylies on 29.01.18.
 */

public class PreferencesManager {

    public enum  RenameRules{
        title_album_artist,
        title_album,
        title_artist,
        title,
        none
    }

    static public void putValue(Context context,String key,Object value) throws IllegalArgumentException{
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        String lol = value.getClass().getSimpleName();
        switch (lol){
            case "String":
                editor.putString(key, String.valueOf(value));
                break;
            case  "Integer":
                editor.putInt(key, (Integer) value);
                break;
            case "Boolean":
                editor.putBoolean(key, (Boolean) value);
                break;
            default:
                throw new IllegalArgumentException("illegal args");
        }
        editor.apply();
    }

    static public String getStringValue(Context context,String key,String defaultValue){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key,defaultValue);
    }
    static public int getIntValue(Context context,String key){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key,Integer.MIN_VALUE);
    }
    static public boolean getBoolValue(Context context,String key){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key,false);
    }
}
