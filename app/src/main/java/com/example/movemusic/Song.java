package com.example.movemusic;

import java.io.Serializable;

public class Song implements Serializable {
    private String songid;
    private String albumimage;
    private String songname;
    private String artistname;

    public String getSongid() {
        return songid;
    }

    public void setSongid(String songid) {
        this.songid = songid;
    }

    public String getAlbumimage() {
        return albumimage;
    }

    public void setAlbumimage(String albumimage) {
        this.albumimage = albumimage;
    }

    public String getSongname() {
        return songname;
    }

    public void setSongname(String songname) {
        this.songname = songname;
    }

    public String getArtistname() {
        return artistname;
    }

    public void setArtistname(String artistname) {
        this.artistname = artistname;
    }

    public Song(String songid, String albumimage, String songname, String artistname) {
        this.songid = songid;
        this.albumimage = albumimage;
        this.songname = songname;
        this.artistname = artistname;
    }
}
