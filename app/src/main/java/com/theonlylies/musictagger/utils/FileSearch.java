package com.theonlylies.musictagger.utils;

import com.theonlylies.musictagger.utils.edit.StorageHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by theonlylies on 23.01.18.
 */

public class FileSearch{

    static public List<File> getFileListing() {
        ArrayList<StorageHelper.MountDevice> list = StorageHelper.getInstance().getAllMountedDevices();
        List<File> result = new ArrayList<>();
        try {
            for(StorageHelper.MountDevice device : list){
                String path = device.getPath();
                if(validateDirectory(new File(path))){
                    result.addAll( getFileListingNoSort(new File(device.getPath())) );
                }

            }
        } catch (NullPointerException e){
            e.printStackTrace();
            return new ArrayList<>();
        }
        //Collections.sort(result);
        return result;
    }

    // PRIVATE

    static private List<File> getFileListingNoSort(File aStartingDir) {
        List<File> result = new ArrayList<>();
        File[] filesAndDirs = aStartingDir.listFiles();
        List<File> filesDirs = Arrays.asList(filesAndDirs);
        for(File file : filesDirs) {
            if(file.isFile() && file.getName().endsWith(".mp3")) result.add(file); //add only mp3 files
            if (! file.isFile()) {
                //must be a directory
                //recursive call!
                List<File> deeperList = getFileListingNoSort(file);
                result.addAll(deeperList);
            }
        }
        return result;
    }

    /**
     * Directory is valid if it exists, does not represent a file, and can be read.
     */
    static private boolean validateDirectory(File aDirectory) {
        return !(aDirectory == null || !aDirectory.exists() || !aDirectory.isDirectory() || !aDirectory.canRead());
    }
}
