package com.theonlylies.musictagger.utils.adapters;

import java.util.List;


/**
 * Created by linuxoid on 22.12.17.
 */

public class BlockItem {
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

    public String getBlockScName() {
        return blockScName;
    }

    public void setBlockScName(String blockScName) {
        this.blockScName = blockScName;
    }

    public List<MusicFile> getMusicFiles() {
        return musicFiles;
    }

    public void setMusicFiles(List<MusicFile> musicFiles) {
        this.musicFiles = musicFiles;
    }

    private String blockName, blockInfo, blockScName;
    public boolean visible=false;
    private List<MusicFile> musicFiles;
}
