package com.theonlylies.musictagger.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.theonlylies.musictagger.utils.adapters.MusicFile;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.AbstractTag;
import org.jaudiotagger.tag.images.AndroidArtwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by Alexander Polumestniy on 29.12.2017.
 */

public class TagManager  {
    private MP3File mp3File;
    private AbstractID3v2Tag V2Tag;

    public TagManager(String path){
        TagOptionSingleton.getInstance().setAndroid(true);

        try {
            mp3File = (MP3File) AudioFileIO.read(new File(path));

            //MP3AudioHeader audioHeader = (MP3AudioHeader) mp3File.getAudioHeader();

            if (mp3File.hasID3v1Tag() && !mp3File.hasID3v2Tag()){
                AbstractTag tag = mp3File.getID3v1Tag();
                mp3File.setID3v2Tag(tag);
            }

            if(mp3File.hasID3v2Tag()){
                V2Tag = mp3File.getID3v2Tag();
            }else{
                V2Tag = (AbstractID3v2Tag) mp3File.createDefaultTag();
                mp3File.setTag(V2Tag);
            }
        } catch (CannotReadException | IOException | ReadOnlyFileException | InvalidAudioFrameException | TagException e) {
            e.printStackTrace();
        }

    }

    public static boolean canRead(String path){
        try {
            MP3File mp3File = (MP3File) AudioFileIO.read(new File(path));
            MP3AudioHeader audioHeader = (MP3AudioHeader)mp3File.getAudioHeader();
            return true;
        } catch (CannotReadException | InvalidAudioFrameException | TagException | IOException e) {
            e.printStackTrace();
            return false;
        } catch (ReadOnlyFileException e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean hasArtwork(){
        try {
            return V2Tag.hasField(FieldKey.COVER_ART);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasTags(){
        return mp3File.hasID3v2Tag();
    }



    //////////////////////////////////////////////////////////////////
    public String getArtist(){
        return getTag(FieldKey.ARTIST);
    }

    public String getAlbum(){
        return getTag(FieldKey.ALBUM);
    }

    public String getTitle(){
        return getTag(FieldKey.TITLE);
    }

    public String getGenre(){
        return getTag(FieldKey.GENRE);
    }

    public String getYear(){
        return getTag(FieldKey.YEAR);
    }

    public String getComment(){
        return getTag(FieldKey.COMMENT);
    }

    public String getDiscNum() {
        return getTag(FieldKey.DISC_NO);
    }

    public String getTrackNum(){
        return getTag(FieldKey.TRACK);
    }

    public String getComposer(){
        return getTag(FieldKey.COMPOSER);
    }

    public String getLyrics(){
        return getTag(FieldKey.LYRICS);
    }



    //////////////////Методы записи//////////////////////////////////////

    public void setArtist(String newContent){
        setTag(FieldKey.ARTIST, newContent);
    }

    public void setAlbum(String newContent){
        setTag(FieldKey.ALBUM,newContent);
    }

    public void setTitle(String newContent){
        setTag(FieldKey.TITLE, newContent);
    }

    public void setGenre(String newContent){
        setTag(FieldKey.GENRE, newContent);
    }

    public void setYear(String newContent){
        setTag(FieldKey.YEAR, newContent);
    }

    public void setComment(String newContent){
        setTag(FieldKey.COMMENT, newContent);
    }

    public void setDiscNum(String newContent) {
        setTag(FieldKey.DISC_NO, newContent);
    }

    public void setTrackNum(String newContent){
        setTag(FieldKey.TRACK, newContent);
    }

    public void setComposer(String newContent){
        setTag(FieldKey.COMPOSER,newContent);
    }
    public void setLyrics(String newContent){
        setTag(FieldKey.LYRICS,newContent);
    }

    /**
     * Set tags form MusicFile instance besides artwork!!!
     * @param musicFile is MusicFile instance
     */
    public void setTagsFromMusicFile(MusicFile musicFile) {
       setAlbum(musicFile.getAlbum());
       setArtist(musicFile.getArtist());
        setTitle(musicFile.getTitle());
        setGenre(musicFile.getGenre());
        setYear(musicFile.getYear());
        setTrackNum(musicFile.getTrackNumber());
    }
    public void setGeneralTagsFromMusicFile(MusicFile musicFile) {
        setAlbum(musicFile.getAlbum());
        setArtist(musicFile.getArtist());
        setGenre(musicFile.getGenre());
        setYear(musicFile.getYear());
        setTrackNum(musicFile.getTrackNumber());
    }

    ///////////////////////////////////////////////////////////////////////////////
    public Bitmap getArtworkAsBitmap(){
            try{
                Bitmap bitmap;
                AndroidArtwork artwork = (AndroidArtwork)V2Tag.getFirstArtwork();
                ByteArrayInputStream stream = new ByteArrayInputStream(artwork.getBinaryData());
                bitmap = BitmapFactory.decodeStream(stream);
                return bitmap;
            }catch (NullPointerException e){
                e.printStackTrace();
                Log.e("TagManager", "Artwork is null");
                return null;
            }
    }

    /////////////////////////////////////////////////////////////////////////////////////

    public void deleteArtwork(){
        V2Tag.deleteArtworkField();
        save();
    }

    public void save() {
        try {
            mp3File.commit();
        } catch (CannotWriteException e) {
            e.printStackTrace();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////

    public boolean setArtwork(Bitmap bitmap) {
        try {
            ByteArrayOutputStream imageArray = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100, imageArray);
            byte[] byteArray = imageArray.toByteArray();
            AndroidArtwork artwork = (AndroidArtwork) ArtworkFactory.getNew();
            V2Tag.deleteArtworkField();
            artwork.setBinaryData(byteArray);
            V2Tag.addField(artwork);
            save();

            return true;
        } catch (FieldDataInvalidException e) {
            e.printStackTrace();
            return false;
        }
    }


/////////////////////////////////////////////////////////////////////////////
    private boolean setTag(FieldKey key, String newContent){
        if(hasTags()){
            try {
                if (V2Tag.hasField(key)){
                    V2Tag.setField(key, newContent);
                }else{
                    //V2Tag.createField(key,newContent);
                    V2Tag.addField(key,newContent);
                }
                save();
                return true;
            }catch (FieldDataInvalidException e) {
                e.printStackTrace();
                return false;
            }
        }else{
            try {
                AbstractID3v2Tag tag = (AbstractID3v2Tag)mp3File.createDefaultTag();
                mp3File.setTag(tag);
                tag.setField(key, newContent);
                save();
                return true;
            } catch (FieldDataInvalidException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    //////////////////////////////////////////////////////////////////////////////////
    private String getTag(FieldKey key){
        try{
            return V2Tag.getFirst(key);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }
}
