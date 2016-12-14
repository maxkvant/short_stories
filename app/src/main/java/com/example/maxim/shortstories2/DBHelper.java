package com.example.maxim.shortstories2;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.maxim.shortstories2.post.Post;
import com.example.maxim.shortstories2.walls.WALL_MODE;
import com.example.maxim.shortstories2.walls.Wall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    public final static String TABLE_POSTS = "Posts";
    public final static String TABLE_WALLS = "Walls";

    private final static int POSTS_PER_GET = 50;
    private final static String DB_NAME = "ShortStoriesDB";
    private final static String wallPath = "com.example.maxim.shortstories2.walls.";

    public DBHelper() {
        super(MyApplication.getInstance(), DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String tablePosts = "create table " + TABLE_POSTS + "(" +
                "id integer primary key," +
                "text text," +
                "wall_id integer," +
                "date integer," +
                "load_date integer," +
                "rating integer)";
        db.execSQL(tablePosts);

        String tableWalls = "create table " + TABLE_WALLS + "(" +
                "id integer primary key," +
                "name text ," +
                "class text," +
                "priority integer)";
        db.execSQL(tableWalls);

        List<String> values = Arrays.asList(MyApplication.getInstance().getBaseContext()
                .getResources().getStringArray(R.array.table_walls_default_items));

        String insertInWallsPrefix = "insert into " + TABLE_WALLS + " values ";

        for (String value : values) {
            String sql = insertInWallsPrefix + value + ";";
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public void insertPosts(List<Post> posts) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        for (Post post : posts) {
            ContentValues values = new ContentValues();
            values.put("id", post.id);
            values.put("text", post.text);
            values.put("wall_id", post.wall_id);
            values.put("date", post.date);
            values.put("rating", post.rating);
            values.put("load_date", post.load_date);
            db.insertWithOnConflict(TABLE_POSTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
        db.endTransaction();
        db.close();
    }

    public List<Post> getPosts(int offset, WALL_MODE mode, String query) {
        List<Post> res = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String queryPrefix = "select * from " + TABLE_POSTS +
                " inner join " + TABLE_WALLS +
                " on " +
                TABLE_POSTS + ".wall_id = " + TABLE_WALLS + ".id " +
                " where 1";

        String querySuffix = getModeSql(mode) +
                " limit " + offset + "," + POSTS_PER_GET + ";";

        String sql = queryPrefix + query + querySuffix;
        Log.d("DBHelper getPosts", sql);
        Cursor cursor = db.rawQuery(queryPrefix + query + querySuffix, null);
        if (cursor.moveToFirst()) {
            do {
                Post post = new Post(
                        cursor.getString(cursor.getColumnIndex("text")),
                        cursor.getLong(cursor.getColumnIndex("wall_id")),
                        cursor.getString(cursor.getColumnIndex("name")),
                        cursor.getInt(cursor.getColumnIndex("date")),
                        cursor.getInt(cursor.getColumnIndex("rating"))
                );
                res.add(post);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return res;
    }

    public List<Wall> getAllWalls() {
        List<Wall> res = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from " + TABLE_WALLS + " order by priority;";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                String className = cursor.getString(cursor.getColumnIndex("class"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                long id = cursor.getLong(cursor.getColumnIndex("id"));
                try {
                    res.add((Wall)Class.forName(wallPath + className)
                            .getConstructor(String.class, long.class)
                            .newInstance(name, id)
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return res;
    }

    private String getModeSql(WALL_MODE mode) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long oneDayBefore = (cal.getTimeInMillis() / 1000);

        String res = "";
        switch (mode) {
            case BY_DATE:
                res = " order by date desc ";
                break;
            case TOP_DAILY:
                cal.add(Calendar.DAY_OF_YEAR,-1);
                res = " and date > " + (cal.getTimeInMillis() / 1000) +
                        " order by rating desc ";
                break;
            case TOP_WEEKLY:
                cal.add(Calendar.WEEK_OF_YEAR, -1);
                res = " and date > " + (cal.getTimeInMillis() / 1000) +
                        " order by rating desc ";
                break;
            case TOP_MONTHLY:
                cal.add(Calendar.MONTH, -1);
                res = " and date > " + (cal.getTimeInMillis() / 1000) +
                        " order by rating desc ";
                break;
        }
        return res;
    }
}
