package com.example.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class dbManager {
    private SQLiteDatabase db;
    private dbHelper db_helper;

    public dbManager(Context context) {
        db_helper = new dbHelper(context);
    }

    public void open() {
        db = db_helper.getReadableDatabase();  // Открытие существующей базы в режиме чтения
    }

    public void close() {
        db_helper.close();
    }

    public int book_number() {
        Cursor cursor = db.rawQuery("PRAGMA table_info(Books)", null);
        int columnCount = cursor.getCount();
        cursor.close();
        return columnCount;
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

    // Функция для получения информации о книге по ID
    public String getBookField(int bookId, String field) {
        Cursor cursor = db.query("Books",
                new String[]{field},  // Колонка, которую хотим получить
                "id=?",               // Условие
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
