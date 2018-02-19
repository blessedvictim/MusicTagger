package com.theonlylies.musictagger.utils.adapters;

import android.net.Uri;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by linuxoid on 20.12.17.
 */

public class MusicFile {
    protected String title;
    protected String artist;
    protected String album;
    protected String realPath;
    protected String genre;
    protected String trackNumber;
    protected String year;
    protected Uri artworkUri;
    public AtomicInteger progress;
    protected long album_id;

    /**
     * set all fields like @file stay unchanged title and realPath
     * @param file instance of MusicFile which fields must be set
     */
    public void setFieldsByMusocFile(MusicFile file){
        this.setAlbum_id(file.getAlbum_id());
        this.setAlbum(file.getAlbum());
        this.setArtist(file.getArtist());
        this.setArtworkUri(file.getArtworkUri());
        this.setGenre(file.getGenre());
        //this.setRealPath(file.getRealPath());
        //this.setTitle(file.getTitle());
        this.setTrackNumber(file.getTrackNumber());
        this.setYear(file.getYear());
    }

    public MusicFile(){
        title=null;
        artist=null;
        album=null;
        realPath=null;
        realPath=null;
        artworkUri=null;
        genre=null;
        trackNumber=null;
        year=null;
        progress=new AtomicInteger();
        progress.set(0);
        album_id=-1;
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

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public long getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(long album_id) {
        this.album_id = album_id;
    }
}
