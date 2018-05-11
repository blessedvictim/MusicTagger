package com.theonlylies.musictagger.utils.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.theonlylies.musictagger.R;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by linuxoid on 20.12.17.
 */

public class MusicFile implements Parcelable {
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
