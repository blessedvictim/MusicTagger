package com.theonlylies.musictagger.utils.adapters;

import android.net.Uri;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by linuxoid on 20.12.17.
 */

public class MusicFile {
    @Getter @Setter protected String title;
    @Getter @Setter protected String artist;
    @Getter @Setter protected String album;
    @Getter @Setter protected String realPath;
    @Getter @Setter protected String genre;
    @Getter @Setter protected String trackNumber;
    @Getter @Setter protected String year;
    @Getter @Setter protected String discNumber;
    @Getter @Setter protected String albumArtist;
    @Getter @Setter protected String comment;
    @Getter @Setter protected String composer;
    @Getter @Setter protected Uri artworkUri;
    @Getter @Setter protected String artworkPath;
    @Getter @Setter public AtomicInteger progress;
    @Getter @Setter protected long album_id;

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

    public static MusicFile createEmpty() {
        MusicFile file = new MusicFile();
        file.title = "empty";
        file.artist = "empty";
        file.album = "empty";
        return file;
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
        artworkPath = null;
    }
}
