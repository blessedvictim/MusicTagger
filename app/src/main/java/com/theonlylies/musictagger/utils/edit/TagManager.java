package com.theonlylies.musictagger.utils.edit;

import android.graphics.Bitmap;

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
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.images.AndroidArtwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import lombok.experimental.var;

/**
 * Created by Alexander Polumestniy on 29.12.2017.
 */

public class TagManager {
    private MP3File mp3File;
    AbstractID3v2Tag tag;

    public TagManager(String path) {
        TagOptionSingleton.getInstance().setAndroid(true);

        try {
            mp3File = (MP3File) AudioFileIO.read(new File(path));


            //MP3AudioHeader audioHeader = (MP3AudioHeader) mp3File.getAudioHeader();

            if (mp3File.hasID3v1Tag() && !mp3File.hasID3v2Tag()) {
                AbstractTag tag = mp3File.getID3v1Tag();
                mp3File.setID3v2Tag(tag);
                this.tag = mp3File.getID3v2Tag();
            } else if (mp3File.hasID3v1Tag()) {
                tag = mp3File.getID3v2Tag();
            }else{
                tag = (AbstractID3v2Tag) mp3File.getTagAndConvertOrCreateAndSetDefault();
            }

        } catch (CannotReadException | IOException | ReadOnlyFileException | InvalidAudioFrameException | TagException e) {
            e.printStackTrace();
        }

    }

    static public MusicFile getMusicFileByPath(String path){
        var tagManager = new TagManager(path);
        var musicFile = new MusicFile();
        musicFile.setComposer(tagManager.getComposer());
        musicFile.setAlbum(tagManager.getAlbum());
        musicFile.setAlbumArtist(tagManager.getAlbumArtist());
        musicFile.setArtist(tagManager.getArtist());
        musicFile.setComment(tagManager.getComment());
        musicFile.setDiscNumber(tagManager.getDiscNum());
        musicFile.setGenre(tagManager.getGenre());
        musicFile.setRealPath(path);
        musicFile.setTitle(tagManager.getTitle());
        musicFile.setTrackNumber(tagManager.getTrackNum());
        musicFile.setYear(tagManager.getYear());
        return musicFile;
    }


    public static void rewriteTag(String path) {
        try {
            MP3File song = new MP3File(new File(path), MP3File.LOAD_ALL);

            if (song.hasID3v2Tag()) {
                AbstractID3v2Tag oritag = song.getID3v2TagAsv24();

                song = new MP3File(new File(path), MP3File.LOAD_ALL);
                oritag = song.getID3v2Tag();
                ID3v24Tag newtag = new ID3v24Tag();
                // copy metadata
                newtag.setField(FieldKey.ARTIST, oritag.getFirst(FieldKey.ARTIST));
                newtag.setField(FieldKey.ALBUM, oritag.getFirst(FieldKey.ALBUM));
                newtag.setField(FieldKey.GENRE, oritag.getFirst(FieldKey.GENRE));
                newtag.setField(FieldKey.TITLE, oritag.getFirst(FieldKey.TITLE));
                newtag.setField(FieldKey.TRACK, oritag.getFirst(FieldKey.TRACK));
                newtag.setField(FieldKey.COMMENT, oritag.getFirst(FieldKey.COMMENT));
                newtag.setField(FieldKey.YEAR, oritag.getFirst(FieldKey.YEAR));
                newtag.setField(FieldKey.ALBUM_ARTIST, oritag.getFirst(FieldKey.ALBUM_ARTIST));
                newtag.setField(FieldKey.COMPOSER, oritag.getFirst(FieldKey.COMPOSER));
                newtag.setField(FieldKey.DISC_NO, oritag.getFirst(FieldKey.DISC_NO));
                if (oritag.hasField(FieldKey.COVER_ART))
                    newtag.setField(oritag.getFirstArtwork());
                song.setID3v2Tag(newtag);
                try {
                    song.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (ReadOnlyFileException | InvalidAudioFrameException | TagException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set tags form MusicFile instance besides artwork!!!
     *
     * @param musicFile is MusicFile instance
     */
    public void setTagsFromMusicFile(MusicFile musicFile) {
        setTitle(musicFile.getTitle());
        this.setGeneralTagsFromMusicFile(musicFile);
    }

    public void setGeneralTagsFromMusicFile(MusicFile musicFile) {
        setAlbum(musicFile.getAlbum());
        setArtist(musicFile.getArtist());
        setGenre(musicFile.getGenre());
        setYear(musicFile.getYear());
        setAlbumArtist(musicFile.getAlbumArtist());
        setComment(musicFile.getComment());
        setComposer(musicFile.getComposer());
        setDiscNum(musicFile.getDiscNumber());
    }


    public static boolean canRead(String path) {
        try {
            MP3File mp3File = (MP3File) AudioFileIO.read(new File(path));
            //MP3AudioHeader audioHeader = (MP3AudioHeader) mp3File.getAudioHeader();
            return true;
        } catch (CannotReadException | InvalidAudioFrameException | TagException | IOException e) {
            e.printStackTrace();
            return false;
        } catch (ReadOnlyFileException e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean hasArtwork() {
        try {
            return mp3File.getID3v2TagAsv24().hasField(FieldKey.COVER_ART);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setArtwork(Bitmap bitmap) {
        try {
            ByteArrayOutputStream imageArray = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageArray);
            byte[] byteArray = imageArray.toByteArray();
            AndroidArtwork artwork = (AndroidArtwork) ArtworkFactory.getNew();
            tag.deleteArtworkField();
            artwork.setBinaryData(byteArray);
            tag.addField(artwork);
            return true;
        } catch (FieldDataInvalidException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasTags() {
        return mp3File.hasID3v2Tag();
    }


    //////////////////////////////////////////////////////////////////
    public String getArtist() {
        return getTag(FieldKey.ARTIST);
    }

    public String getAlbum() {
        return getTag(FieldKey.ALBUM);
    }

    public String getTitle() {
        return getTag(FieldKey.TITLE);
    }

    public String getGenre() {
        return getTag(FieldKey.GENRE);
    }

    public String getYear() {
        return getTag(FieldKey.YEAR);
    }

    public String getComment() {
        return getTag(FieldKey.COMMENT);
    }

    public String getDiscNum() {
        return getTag(FieldKey.DISC_NO);
    }

    public String getTrackNum() {
        return getTag(FieldKey.TRACK);
    }

    public String getComposer() {
        return getTag(FieldKey.COMPOSER);
    }

    public String getAlbumArtist(){return getTag(FieldKey.ALBUM_ARTIST);}

    public String getLyrics() {
        return getTag(FieldKey.LYRICS);
    }



    //////////////////Методы записи//////////////////////////////////////

    public void setArtist(String newContent) {
        setTag(FieldKey.ARTIST, newContent);
    }

    public void setAlbum(String newContent) {
        setTag(FieldKey.ALBUM, newContent);
    }

    public void setTitle(String newContent) {
        setTag(FieldKey.TITLE, newContent);
    }

    public void setGenre(String newContent) {
        setTag(FieldKey.GENRE, newContent);
    }

    public void setYear(String newContent) {
        setTag(FieldKey.YEAR, newContent);
    }

    public void setComment(String newContent) {
        setTag(FieldKey.COMMENT, newContent);
    }

    public void setDiscNum(String newContent) {
        setTag(FieldKey.DISC_NO, newContent);
    }

    public void setTrackNum(String newContent) {
        setTag(FieldKey.TRACK, newContent);
    }

    public void setComposer(String newContent) {
        setTag(FieldKey.COMPOSER, newContent);
    }

    public void setLyrics(String newContent) {
        setTag(FieldKey.LYRICS, newContent);
    }

    public void setAlbumArtist(String newContent){
        setTag(FieldKey.ALBUM_ARTIST, newContent);
    }


    /////////////////////////////////////////////////////////////////////////////////////

    public void deleteArtwork() {
        tag.deleteArtworkField();
    }

    public void save() {
        try {
            mp3File.commit();
        } catch (CannotWriteException e) {
            e.printStackTrace();
        }
    }


    /////////////////////////////////////////////////////////////////////////////
    private boolean setTag(FieldKey key, String newContent) {
        if (newContent==null)newContent="";
        try {
            if (tag.hasField(key)) {
                tag.setField(key, newContent);
            } else {
                tag.addField(key, newContent);
            }
            return true;
        } catch (FieldDataInvalidException e) {
            e.printStackTrace();
            return false;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    private String getTag(FieldKey key) {
        try {
            return mp3File.getID3v2TagAsv24().getFirst(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void printInfo(String path) {
        try {
            MP3File song = new MP3File(new File(path), MP3File.LOAD_ALL);

            if (song.hasID3v2Tag()) {
                AbstractID3v2Tag oritag;
                oritag = song.getID3v2TagAsv24();
                // copy metadata

                for (FieldKey field : FieldKey.values()) {
                    System.out.println(String.valueOf(field) + " : " + oritag.getFirst(field));
                }
            } else {
                System.out.println("havent tags");
            }

        } catch (ReadOnlyFileException | InvalidAudioFrameException | IOException | TagException e) {
            e.printStackTrace();
        }
    }

    public static void printAdvancedInfo(String path) {
        try {
            MP3File song = new MP3File(new File(path), MP3File.LOAD_ALL);

            if (song.hasID3v2Tag()) {
                AbstractID3v2Tag oritag;
                oritag = song.getID3v2TagAsv24();
                // copy metadata

                for (FieldKey field : FieldKey.values()) {
                    System.out.println(field + " : " + oritag.getFirst(field));
                }
            } else {
                System.out.println("havent tags");
            }

        } catch (ReadOnlyFileException | InvalidAudioFrameException | IOException | TagException e) {
            e.printStackTrace();
        }
    }
}


