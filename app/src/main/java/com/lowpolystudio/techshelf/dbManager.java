package com.lowpolystudio.techshelf;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class dbManager {
    private SQLiteDatabase db;
    private dbHelper db_helper;

    public dbManager(Context context) {
        db_helper = new dbHelper(context);
    }

    public void open() {
        db = db_helper.getReadableDatabase();
    }

    public void close() {
        db_helper.close();
    }

    public int bookNumber() {
        Cursor cursor = db.rawQuery("SELECT * FROM Books", null);
        int columnCount = cursor.getCount();
        cursor.close();
        return columnCount;
    }

    public String getBookTags(int bookId) {
        List<String> tags = new ArrayList<>();

        String sql = "SELECT Tags.Name FROM Tags " +
                "JOIN BookTags ON Tags.ID = BookTags.TagID " +
                "WHERE BookTags.BookID = ?";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(bookId)});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                tags.add(cursor.getString(0));
            }
            cursor.close();
        }

        String result = "";

        for (int i = 0; i < tags.size(); i++)
            if (i < tags.size() - 1)
                result += tags.get(i) + ", ";
            else
                result += tags.get(i);

        return result;
    }

    public String getBookCover(int bookId) {
        Cursor cursor = db.query("Books",
                new String[]{"ImgName"},
                "id = ?",
                new String[]{String.valueOf(bookId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String coverImage = cursor.getString(cursor.getColumnIndexOrThrow("ImgName"));
            cursor.close();
            return coverImage;
        }
        return null;
    }

    public String getBookField(int bookId, String field) {
        Cursor cursor = db.query("Books",
                new String[]{field},
                "id=?",
                new String[]{String.valueOf(bookId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String result = cursor.getString(cursor.getColumnIndexOrThrow(field));
            cursor.close();
            return result;
        } else {
            return null;
        }
    }
}
