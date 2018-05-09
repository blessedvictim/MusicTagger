package com.theonlylies.musictagger.utils.adapters;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by theonlylies on 05.01.18.
 */

public class ParcelableMusicFile extends MusicFile implements Parcelable {

    protected ParcelableMusicFile(Parcel in) {
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        realPath = in.readString();
        genre = in.readString();
        trackNumber = in.readString();
        year = in.readString();
        //String uri = in.readString();
        /*if(uri==null || uri.isEmpty())artworkUri=null;
        else artworkUri = Uri.parse( uri);*/
        album_id = in.readLong();
    }

    public ParcelableMusicFile(MusicFile in) {
        title = in.getTitle();
        artist = in.getArtist();
        album = in.getAlbum();
        realPath = in.getRealPath();
        genre = in.getGenre();
        trackNumber = in.getTrackNumber();
        year = in.getYear();
        artworkUri = in.getArtworkUri();
        album_id = in.getAlbum_id();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeString(realPath);
        dest.writeString(genre);
        dest.writeString(trackNumber);
        dest.writeString(year);
        /*if(artworkUri==null)dest.writeString(null);
        else dest.writeString(artworkUri.toString());*/

        dest.writeLong(album_id);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MusicFile> CREATOR = new Parcelable.Creator<MusicFile>() {
        @Override
        public MusicFile createFromParcel(Parcel in) {
            return new ParcelableMusicFile(in);
        }

        @Override
        public MusicFile[] newArray(int size) {
            return new MusicFile[size];
        }
    };
}