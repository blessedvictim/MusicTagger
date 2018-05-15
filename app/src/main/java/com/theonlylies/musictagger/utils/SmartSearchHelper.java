package com.theonlylies.musictagger.utils;

import android.net.Uri;

import com.theonlylies.musictagger.utils.adapters.MusicFile;

import Fox.core.lib.general.data.ID3V2;

public class SmartSearchHelper {
    static public void ID3V2inMusicFile(ID3V2 tags, MusicFile musicFile) {
        musicFile.setAlbum(tags.getAlbum());
        musicFile.setArtist(tags.getArtist());
        musicFile.setTitle(tags.getTitle());
        musicFile.setYear(tags.getYear());
        musicFile.setTrackNumber(String.valueOf(tags.getNumber()));
        musicFile.setComment(tags.getComment());
        if (tags.getArtLinks() != null && !tags.getArtLinks().isEmpty()) {
            musicFile.setArtworkUri(Uri.parse(tags.getArtLinks().get(0)));
        }
    }
}
