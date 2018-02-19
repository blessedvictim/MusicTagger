package com.theonlylies.musictagger.utils.adapters;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by linuxoid on 22.12.17.
 */

public class BlockItem {
    private String blockName,blockInfo,blockScName;
    public boolean visible=false;
    private List<MusicFile> musicFiles;


    public BlockItem(){
        musicFiles = new LinkedList();
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public String getBlockInfo() {
        return blockInfo;
    }

    public void setBlockInfo(String blockInfo) {
        this.blockInfo = blockInfo;
    }

    public List<MusicFile> getMusicFiles() {
        return musicFiles;
    }

    public void setMusicFiles(List<MusicFile> musicFiles) {
        this.musicFiles = musicFiles;
    }

    public String getBlockScName() {
        return blockScName;
    }

    public void setBlockScName(String blockScName) {
        this.blockScName = blockScName;
    }
}
