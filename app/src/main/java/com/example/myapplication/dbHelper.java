package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class dbHelper extends android.database.sqlite.SQLiteOpenHelper {
    private static final String DATABASE_NAME = "database.sqlite";
    private static final int DATABASE_VERSION = 1;
    private static String DB_PATH = "";
    private final Context context;

    public dbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        copyDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Таблица уже существует, пропускаем создание
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        context.deleteDatabase(DATABASE_NAME);
        copyDatabase();
    }

    // Копирование базы данных из assets в директорию приложения
    private void copyDatabase() {
        if (!checkDatabase()) {
            this.getReadableDatabase();
            try {
                InputStream input = context.getAssets().open(DATABASE_NAME);
                String outFileName = DB_PATH + DATABASE_NAME;
                OutputStream output = new FileOutputStream(outFileName);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }

                output.flush();
                output.close();
                input.close();
            } catch (IOException e) {
                Log.e("DatabaseHelper", "Ошибка копирования базы данных: " + e.getMessage());
            }
        }
    }

    // Проверка, существует ли база данных
    private boolean checkDatabase() {
        SQLiteDatabase checkDB = null;
        try {
            String path = DB_PATH + DATABASE_NAME;
            checkDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            Log.e("DatabaseHelper", "База данных отсутствует.");
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;
    }
}
