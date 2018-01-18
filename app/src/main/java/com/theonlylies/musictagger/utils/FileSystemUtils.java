package com.theonlylies.musictagger.utils;

import android.util.Log;

import java.io.File;

/**
 * Created by theonlylies on 02.01.18.
 */

public class FileSystemUtils {

    static public boolean onSdCardFile(String path){
        StorageHelper storageHelper = StorageHelper.getInstance();
        for(StorageHelper.MountDevice mountDevice : storageHelper.getAllMountedDevices()){
            if (mountDevice.getType()== StorageHelper.MountDeviceType.REMOVABLE_SD_CARD){
                Log.d("EXTSDCARD:",mountDevice.getPath());
                if(path.startsWith(mountDevice.getPath())){
                    return true;
                }

            }
        }
        return false;
    }
}
