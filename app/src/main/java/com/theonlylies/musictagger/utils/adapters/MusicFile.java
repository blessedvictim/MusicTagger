package com.theonlylies.musictagger.utils.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.theonlylies.musictagger.R;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by linuxoid on 20.12.17.
 */

public class MusicFile implements Parcelable {
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

    public String getDiscNumber() {
        return discNumber;
    }

    public void setDiscNumber(String discNumber) {
        this.discNumber = discNumber;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public Uri getArtworkUri() {
        return artworkUri;
    }

    public void setArtworkUri(Uri artworkUri) {
        this.artworkUri = artworkUri;
    }

    public String getArtworkPath() {
        return artworkPath;
    }

    public void setArtworkPath(String artworkPath) {
        this.artworkPath = artworkPath;
    }

    public AtomicInteger getProgress() {
        return progress;
    }

    public void setProgress(AtomicInteger progress) {
        this.progress = progress;
    }

    public long getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(long album_id) {
        this.album_id = album_id;
    }

    protected String title;
    protected String artist;
    protected String album;
    protected String realPath;
    protected String genre;
    protected String trackNumber;
    protected String year;
    protected String discNumber;
    protected String albumArtist;
    protected String comment;
    protected String composer;
    protected Uri artworkUri;
    protected String artworkPath;
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

    public static MusicFile createEmpty(Context context) {
        MusicFile file = new MusicFile();
        file.title = "empty";
        file.artist = "empty";
        file.album = "empty";
        file.artworkUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.vector_artwork_placeholder);
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.artist);
        dest.writeString(this.album);
        dest.writeString(this.realPath);
        dest.writeString(this.genre);
        dest.writeString(this.trackNumber);
        dest.writeString(this.year);
        dest.writeString(this.discNumber);
        dest.writeString(this.albumArtist);
        dest.writeString(this.comment);
        dest.writeString(this.composer);
        dest.writeString(this.artworkPath);
        dest.writeLong(this.album_id);
        dest.writeParcelable(this.artworkUri, flags);
    }

    protected MusicFile(Parcel in) {
        this.title = in.readString();
        this.artist = in.readString();
        this.album = in.readString();
        this.realPath = in.readString();
        this.genre = in.readString();
        this.trackNumber = in.readString();
        this.year = in.readString();
        this.discNumber = in.readString();
        this.albumArtist = in.readString();
        this.comment = in.readString();
        this.composer = in.readString();
        this.artworkPath = in.readString();
        this.album_id = in.readLong();
        this.artworkUri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Parcelable.Creator<MusicFile> CREATOR = new Parcelable.Creator<MusicFile>() {
        @Override
        public MusicFile createFromParcel(Parcel source) {
            return new MusicFile(source);
        }

        @Override
        public MusicFile[] newArray(int size) {
            return new MusicFile[size];
        }
    };
}
