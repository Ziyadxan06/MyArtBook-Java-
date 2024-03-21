package com.ziyad.myartbook.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Art {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "artname")
    public String artName;

    @ColumnInfo(name = "artistname")
    public String artistName;

    @ColumnInfo(name = "year")
    public String year;

    @ColumnInfo(name = "image")
    public byte[] image;

    public Art(String artName, String artistName, String year, byte[] image) {
        this.artName = artName;
        this.artistName = artistName;
        this.year = year;
        this.image = image;
    }
}
