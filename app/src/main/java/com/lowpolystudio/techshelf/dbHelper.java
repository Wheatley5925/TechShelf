package com.lowpolystudio.techshelf;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class dbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "database.sqlite";
    private static final int DATABASE_VERSION = 1;
    private static String DB_PATH;
    private final Context context;

    public dbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        copyDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Database already exists, skipping creation
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        context.deleteDatabase(DATABASE_NAME);
        copyDatabase();
    }

    private void copyDatabase() {
        if (!checkDatabase()) {
            this.getReadableDatabase();
            performCopy();
        } else {
            if (isDatabaseDifferent()) {
                Log.i("DatabaseHelper", "Database differs. Updating...");
                context.deleteDatabase(DATABASE_NAME);
                performCopy();
            } else {
                Log.i("DatabaseHelper", "Database is up to date.");
            }
        }
    }

    private boolean checkDatabase() {
        SQLiteDatabase checkDB = null;
        try {
            String path = DB_PATH + DATABASE_NAME;
            checkDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            Log.e("DatabaseHelper", "Database does not exist.");
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;
    }

    private void performCopy() {
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
            Log.e("DatabaseHelper", "Error copying database: " + e.getMessage());
        }
    }

    private boolean isDatabaseDifferent() {
        try {
            InputStream assetInputStream = context.getAssets().open(DATABASE_NAME);
            String assetHash = calculateMD5(assetInputStream);

            InputStream dbInputStream = new FileInputStream(DB_PATH + DATABASE_NAME);
            String dbHash = calculateMD5(dbInputStream);

            return assetHash != null && !assetHash.equals(dbHash);
        } catch (IOException e) {
            Log.e("DatabaseHelper", "Error comparing databases: " + e.getMessage());
            return true; // Assume different if comparison fails.
        }
    }

    private String calculateMD5(InputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            DigestInputStream dis = new DigestInputStream(inputStream, digest);
            byte[] buffer = new byte[1024];
            while (dis.read(buffer) > 0) {}
            dis.close();

            byte[] md5Bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : md5Bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "MD5 calculation error: " + e.getMessage());
            return null;
        }
    }
}
