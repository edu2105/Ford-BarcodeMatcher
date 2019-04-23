package com.example.android.barcodematcher.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by HSANHUES on 6/18/2018.
 */

public final class BarcodeContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.barcodematcher";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_BARCODES = "barcodes";

    private BarcodeContract() {
    }

    public static final class BarcodeEntity implements BaseColumns {

        public static final String TABLE_NAME = "barcodes";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BARCODES);

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_CODEBAR_GRAVITY = "gravity";
        public static final String COLUMN_CODEBAR_PART = "part";
        public static final String COLUMN_CODEBAR_STATUS = "status";
        public static final String COLUMN_CODEBAR_TIMESTAMP = "dateTime";
    }
}
