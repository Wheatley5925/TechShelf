package com.lowpolystudio.techshelf;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class dbManager {
    private SQLiteDatabase db;
    private final dbHelper db_helper;

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
        while (cursor.moveToNext()) {
            tags.add(cursor.getString(0));
        }
        cursor.close();

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < tags.size(); i++)
            if (i < tags.size() - 1)
                result.append(tags.get(i)).append(", ");
            else
                result.append(tags.get(i));

        return result.toString();
    }

    public String getBookCover(int bookId) {
        Cursor cursor = db.query("Books",
                new String[]{"ImgName"},
                "id = ?",
                new String[]{String.valueOf(bookId)},
                null, null, null);

        if (cursor.moveToFirst()) {
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

        if (cursor.moveToFirst()) {
            String result = cursor.getString(cursor.getColumnIndexOrThrow(field));
            cursor.close();
            return result;
        } else {
            return null;
        }
    }

    public List<String> getTagsByType(String type) {
        List<String> list = new ArrayList<>();
        Cursor c = db.query(
                "Tags",
                new String[]{"Name"},
                "Type = ?",
                new String[]{type},
                null, null, "Name"
        );
        while (c.moveToNext()) {
            list.add(c.getString(0));
        }
        c.close();
        return list;
    }

    public List<Integer> getBookIdsByTags(String tag1, String tag2) {
        String query = "SELECT b.ID FROM Books b " +
                "JOIN BookTags bt1 ON b.ID = bt1.BookID " +
                "JOIN Tags t1 ON bt1.TagID = t1.ID " +
                "JOIN BookTags bt2 ON b.ID = bt2.BookID " +
                "JOIN Tags t2 ON bt2.TagID = t2.ID " +
                "WHERE t1.NAME = ? AND t2.NAME = ? " +
                "GROUP BY b.ID";

        Cursor cursor = db.rawQuery(query, new String[]{tag1, tag2});
        List<Integer> bookIds = new ArrayList<>();
        while (cursor.moveToNext()) {
            bookIds.add(cursor.getInt(0));
        }
        cursor.close();
        return bookIds;
    }

    public void loadTagsByType(String type, LinearLayout container, List<CheckBox> checkList) {
        Cursor cursor = db.query("Tags", new String[]{"Name"}, "Type = ?", new String[]{type}, null, null, "Name");
        while (cursor.moveToNext()) {
            String tag = cursor.getString(0);
            CheckBox cb = new CheckBox(container.getContext());
            cb.setText(tag);
            container.addView(cb);
            checkList.add(cb);
        }
        cursor.close();
    }

    private String makePlaceholders(int count) {
        return TextUtils.join(",", Collections.nCopies(count, "?"));
    }

    public List<Integer> getBookIdsForPreferences(List<String> languages, List<String> purposes) {
        List<Integer> result = new ArrayList<>();
        if (languages.isEmpty() || purposes.isEmpty()) return result;

        String langPlaceholders = makePlaceholders(languages.size());
        String purpPlaceholders = makePlaceholders(purposes.size());

        String sql =
                "SELECT DISTINCT bt1.BookID " +
                        "FROM BookTags bt1 " +
                        "JOIN Tags t1 ON bt1.TagID = t1.ID AND t1.Type = 'language' " +
                        "JOIN BookTags bt2 ON bt1.BookID = bt2.BookID " +
                        "JOIN Tags t2 ON bt2.TagID = t2.ID AND t2.Type = 'purpose' " +
                        "WHERE t1.Name IN (" + langPlaceholders + ") " +
                        "AND t2.Name IN (" + purpPlaceholders + ")";

        List<String> args = new ArrayList<>();
        args.addAll(languages);  // languages first
        args.addAll(purposes);   // purposes second

        Cursor cursor = db.rawQuery(sql, args.toArray(new String[0]));
        while (cursor.moveToNext()) {
            result.add(cursor.getInt(0));
        }
        cursor.close();

        return result;
    }
}
