package com.example.maxim.shortstories2.post;

import android.provider.BaseColumns;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class Post implements Serializable {
    public final String text;
    public final long wall_id;
    public final String wall_name;
    public final int date;
    public final int load_date;
    public final double rating;
    public final int id;

    public Post(String text, long wall_id, String wall_name, int date, double rating) {
        this.text = text;
        this.wall_id = wall_id;
        this.wall_name = wall_name;
        this.date = date;
        this.rating = rating;
        this.load_date = (int)(new Date().getTime() / 1000);
        this.id = text.hashCode();
    }

    public final static class PostsEntry implements BaseColumns {
        final public static String TABLE_NAME = "Posts";
        final public static String COLUMN_NAME_ID = "id";
        final public static String COLUMN_NAME_TEXT = "text";
        final public static String COLUMN_NAME_WALL_ID = "wall_id";
        final public static String COLUMN_NAME_DATE = "date";
        final public static String COLUMN_NAME_LOAD_DATE = "load_date";
        final public static String COLUMN_NAME_RATING = "rating";
    }
}
