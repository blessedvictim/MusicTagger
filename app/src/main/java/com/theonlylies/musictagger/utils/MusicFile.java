package com.theonlylies.musictagger.utils;

import android.net.Uri;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by linuxoid on 20.12.17.
 */

public class MusicFile {
    private String title;
    private String artist;
    private String album;
    private String realPath;
    private Uri artworkUri;
    public AtomicInteger progress;

    public MusicFile(){
        title=null;
        artist=null;
        album=null;
        realPath=null;
        realPath=null;
        artworkUri=null;
        progress=new AtomicInteger();
        progress.set(0);

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getRealPath() {
        return realPath;
    }

    public void setRealPath(String realPath) {
        this.realPath = realPath;
    }

    public Uri getArtworkUri() {
        return artworkUri;
    }

    public void setArtworkUri(Uri artworkUri) {
        this.artworkUri = artworkUri;
    }

}
