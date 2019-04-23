package com.example.android.barcodematcher.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.barcodematcher.R;

/**
 * Created by HSANHUES on 6/18/2018.
 */

public class BarcodeProvider extends ContentProvider {

    private BarcodeDBHelper barcodeDBHelper;

    private static final int BARCODES = 100;
    private static final int BARCODE_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(BarcodeContract.CONTENT_AUTHORITY, BarcodeContract.PATH_BARCODES, BARCODES);
        sUriMatcher.addURI(BarcodeContract.CONTENT_AUTHORITY, BarcodeContract.PATH_BARCODES + "/#", BARCODE_ID);
    }

    @Override
    public boolean onCreate() {
        barcodeDBHelper = new BarcodeDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = barcodeDBHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch(match) {
            case BARCODES:
                cursor = database.query(BarcodeContract.BarcodeEntity.TABLE_NAME,projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case BARCODE_ID:
                selection = BarcodeContract.BarcodeEntity._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(BarcodeContract.BarcodeEntity.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown Uri: " + uri);
        }
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case BARCODES:
                return insertBarcode(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    private Uri insertBarcode(Uri uri, ContentValues values) {
        // Check that the gravity Barcode is not null
        String gravityBarcode = values.getAsString(BarcodeContract.BarcodeEntity.COLUMN_CODEBAR_GRAVITY);
        if (gravityBarcode == null || gravityBarcode.isEmpty() || gravityBarcode.equals(getContext().getResources().getString(R.string.gravity_barcode_error))) {
            throw new IllegalArgumentException("" + R.string.empty_db_gravity_barcode);
        }

        // Check that the name is not null
        String partBarcode = values.getAsString(BarcodeContract.BarcodeEntity.COLUMN_CODEBAR_PART);
        if (partBarcode == null || partBarcode.isEmpty() || partBarcode.equals(getContext().getResources().getString(R.string.part_barcode_error))) {
            throw new IllegalArgumentException("" + R.string.empty_db_part_barcode);
        }


        // No need to check the breed, any value is valid (including null).
        SQLiteDatabase database = barcodeDBHelper.getWritableDatabase();

        long id = database.insert(BarcodeContract.BarcodeEntity.TABLE_NAME, null, values);

        if(id == -1) {
            return null;
        }

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }
}
