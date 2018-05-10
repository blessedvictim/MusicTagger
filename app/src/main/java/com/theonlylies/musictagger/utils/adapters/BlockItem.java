package com.theonlylies.musictagger.utils.adapters;

import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by linuxoid on 22.12.17.
 */

public class BlockItem {
    @Getter @Setter private String blockName,blockInfo,blockScName;
    public boolean visible=false;
    @Getter @Setter private List<MusicFile> musicFiles;
}
