package com.example.android.barcodematcher;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.example.android.barcodematcher.data.BarcodeContract;

/**
 * Created by HSANHUES on 6/19/2018.
 */

public class BarcodeHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_history_activity);

        displayDatabaseInfo();
    }

    private void displayDatabaseInfo(){
        String[] projection = {
                BarcodeContract.BarcodeEntity._ID,
                BarcodeContract.BarcodeEntity.COLUMN_CODEBAR_GRAVITY,
                BarcodeContract.BarcodeEntity.COLUMN_CODEBAR_PART,
                BarcodeContract.BarcodeEntity.COLUMN_CODEBAR_STATUS,
                BarcodeContract.BarcodeEntity.COLUMN_CODEBAR_TIMESTAMP};

        Cursor cursor = getContentResolver().query(
                BarcodeContract.BarcodeEntity.CONTENT_URI,
                projection,
                null,
                null,
                null);

        ListView barcodeListView = (ListView) findViewById(R.id.list);
        View emptyImage = findViewById(R.id.empty_view);
        barcodeListView.setEmptyView(emptyImage);

        BarcodeCursorAdapter adapter = new BarcodeCursorAdapter(this, cursor);

        barcodeListView.setAdapter(adapter);

        }
}
