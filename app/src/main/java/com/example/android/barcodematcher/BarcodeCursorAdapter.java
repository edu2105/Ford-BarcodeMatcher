package com.example.android.barcodematcher;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.barcodematcher.data.BarcodeContract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by HSANHUES on 6/19/2018.
 */

public class BarcodeCursorAdapter extends CursorAdapter {

    public BarcodeCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView gravityTextView = (TextView) view.findViewById(R.id.gravity_text_view);
        ImageView statusImageView = (ImageView) view.findViewById(R.id.status_image_view);
        TextView partTextView = (TextView) view.findViewById(R.id.part_text_view);
        TextView timeStampTextView = (TextView) view.findViewById(R.id.date_time_text_view);

        int gravityColumnIndex = cursor.getColumnIndex(BarcodeContract.BarcodeEntity.COLUMN_CODEBAR_GRAVITY);
        int partColumnIndex = cursor.getColumnIndex(BarcodeContract.BarcodeEntity.COLUMN_CODEBAR_PART);
        int imageColumnIndex = cursor.getColumnIndex(BarcodeContract.BarcodeEntity.COLUMN_CODEBAR_STATUS);
        int dateColumnIndex = cursor.getColumnIndex(BarcodeContract.BarcodeEntity.COLUMN_CODEBAR_TIMESTAMP);

        String gravityBarcodeNumber = cursor.getString(gravityColumnIndex);
        String partBarcodeNumber = cursor.getString(partColumnIndex);
        int imageStatus = cursor.getInt(imageColumnIndex);
        String dateTimeStamp = cursor.getString(dateColumnIndex);
       dateTimeStamp = convertDateTime(dateTimeStamp);

        gravityTextView.setText(gravityBarcodeNumber);
        if(imageStatus == 0)
            statusImageView.setImageResource(R.drawable.nook_icon);
        else
            statusImageView.setImageResource(R.drawable.ok_icon);
        partTextView.setText(partBarcodeNumber);
        timeStampTextView.setText(dateTimeStamp);
    }

    private String convertDateTime(String dateTime) {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat outputFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault());
        outputFormat.setTimeZone(TimeZone.getDefault());

        Date date;
        String outputString = null;

        try {
            date = inputFormat.parse(dateTime);
            outputString = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return outputString;
    }
}
