package com.example.android.barcodematcher.data;

import android.content.Context;
import com.example.android.barcodematcher.data.BarcodeContract.BarcodeEntity;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by HSANHUES on 6/18/2018.
 */

public class BarcodeDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "history.db";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + BarcodeEntity.TABLE_NAME;
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + BarcodeEntity.TABLE_NAME + " (" +
                    BarcodeEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    BarcodeEntity.COLUMN_CODEBAR_GRAVITY + " TEXT NOT NULL, " +
                    BarcodeEntity.COLUMN_CODEBAR_PART + " TEXT NOT NULL, " +
                    BarcodeEntity.COLUMN_CODEBAR_STATUS + " INTEGER DEFAULT 0, " +
                    BarcodeEntity.COLUMN_CODEBAR_TIMESTAMP + " DATETIME DEFAULT (datetime('now','localtime')));";
    public BarcodeDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }
}
