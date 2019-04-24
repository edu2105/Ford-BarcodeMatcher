package com.example.android.barcodematcher;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.android.barcodematcher.data.BarcodeContract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {

//    private BarcodeDetector detector;
    private EditText gravityBarcodeEditText;
    private EditText partBarcodeEditText;
    private ImageView okImage;
    private ImageView notOkImage;
    private ImageView barcodeImage;
    private ImageView activeHarnessPosImage;
    private ImageView partPosition;
    private boolean isGravityNeedToWatch;
    private boolean isPartNeedToWatch;
    private boolean isResultOk;
    private Runnable input_finish_checker;
    private long last_text_edit = 0;
    private long delay = 50;
    private Handler handler = new Handler();
    private String doorHarnessesLineUp;
    private String[] gravityParts;
    private String[] matrix;
    private String errorState = ERROR404;
    private static final int NOMATCH = 0;
    private static final int MATCH = 1;
    private static final int MAX_COLUMNS = 25;
    private static final int MAX_ROWS = 4;
    private static final String ERROR404 = "404";
    private static final String ERRORIMAGE = "ImageError";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        readFromTxt();
        populateGravity();
        initValues();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.history_item:
                Intent intent = new Intent(this, BarcodeHistoryActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void readFromTxt() {
        StringBuilder text = new StringBuilder();
        try{
            File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File file = new File(dcim, "GravityLineUp.txt");

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null) {
                text.append(line);
                text.append('*');
            }
            br.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
        doorHarnessesLineUp = text.toString();
    }

    private void populateGravity() {
        int columns;
        int rows;
        int indexColumns = 11;
        int indexRows = doorHarnessesLineUp.indexOf("Filas") + 8;
        int[] columnsName = new int [MAX_COLUMNS];
        int[] rowsName = new int [MAX_ROWS];
        int[] matrixIndex;
        int auxMatrix = 0;
        int auxMatrixIndex = 0;

        columns = Integer.valueOf(doorHarnessesLineUp.substring(indexColumns, indexColumns + 2));
        rows = Integer.valueOf(doorHarnessesLineUp.substring(indexRows, indexRows + 2));

        matrix = new String[columns * rows];
        matrixIndex = new int[columns * rows];
        gravityParts = new String[columns * rows];

        for(int r=0; r<rows; r++) {
            rowsName[r] = r + 1;
           for(int c=0; c<columns; c++) {
               columnsName[c] = 65 + c;
               matrix[auxMatrix] = "" + (char) (columnsName[c] + 32) + "" +
                       String.valueOf(rowsName[r]);
               matrixIndex[auxMatrixIndex] = doorHarnessesLineUp.indexOf(
                       "*" + (char) columnsName[c] + rowsName[r] + "*");
               auxMatrix++;
               auxMatrixIndex++;
           }
        }

        for(int l=0; l<gravityParts.length - 1; l++) {
            gravityParts[l] = doorHarnessesLineUp.substring(matrixIndex[l] + 4, matrixIndex[l+1]);
        }

        gravityParts[gravityParts.length - 1] = doorHarnessesLineUp.substring(
                matrixIndex[matrixIndex.length - 1] + 4,
                doorHarnessesLineUp.length() - 1);

    }

    private void initValues() {

        isResultOk = true;
        isGravityNeedToWatch = false;
        isPartNeedToWatch = true;

        RelativeLayout imagesRelativeLayout;
        imagesRelativeLayout = (RelativeLayout) findViewById(R.id.images_relative_layout);
        imagesRelativeLayout.setVisibility(VISIBLE);

        barcodeImage = (ImageView) findViewById(R.id.barcode_image_view);
        barcodeImage.setVisibility(VISIBLE);

        okImage = (ImageView) findViewById(R.id.pass_image_view);
        okImage.setVisibility(GONE);

        notOkImage = (ImageView) findViewById(R.id.reject_image_view);
        notOkImage.setVisibility(GONE);

        partPosition = (ImageView) findViewById(R.id.pos_to_show);
        partPosition.setVisibility(GONE);

        activeHarnessPosImage = null;
        partPosition.setVisibility(GONE);


        gravityBarcodeEditText = (EditText) findViewById(R.id.gravity_part_barcode_container_edit_text);
        partBarcodeEditText = (EditText) findViewById(R.id.part_barcode_container_edit_text);
        gravityBarcodeEditText.setFocusable(false);
        gravityBarcodeEditText.setBackgroundResource(R.drawable.barcode_number_text_view_grey_background);

        input_finish_checker = new Runnable() {
            @Override
            public void run() {
                if((System.currentTimeMillis() > (last_text_edit + delay - 500)) &&
                        isPartNeedToWatch) {
                    String codeScanned = partBarcodeEditText.getText().toString();
                    if (validatePartCode(codeScanned)) {
                        gravityBarcodeEditText.setBackgroundResource(R.drawable.barcode_number_text_view_background);
                        gravityBarcodeEditText.setFocusable(true);
                        gravityBarcodeEditText.setFocusableInTouchMode(true);
                        partBarcodeEditText.setFocusable(false);
                        partBarcodeEditText.clearFocus();
                        partBarcodeEditText.setBackgroundResource(R.drawable.barcode_number_text_view_grey_background);
                        isPartNeedToWatch = false;
                        isGravityNeedToWatch = true;
                        handler.removeCallbacks(input_finish_checker);
                    }else {
                        partBarcodeEditText.setText("");
                        handler.removeCallbacks(input_finish_checker);
                    }
                } else if((System.currentTimeMillis() > (last_text_edit + delay - 500)) &&
                        isGravityNeedToWatch) {
                    String codeScanned = gravityBarcodeEditText.getText().toString();
                    if(validateGravityPartCode(codeScanned)) {
                        isGravityNeedToWatch = false;
                        gravityBarcodeEditText.setBackgroundResource(R.drawable.barcode_number_text_view_grey_background);
                        gravityBarcodeEditText.setFocusable(false);
                        if(codeScanned.equals("-")) {
                            gravityBarcodeEditText.setText(codeScanned.substring(
                                    0,
                                    codeScanned.length()));
                        } else {
                            gravityBarcodeEditText.setText(codeScanned.substring(
                                    0,
                                    codeScanned.length()-1));
                        }
                        gravityBarcodeEditText.clearFocus();
                        checkCompatibility(activeHarnessPosImage);
                    } else {
                        gravityBarcodeEditText.setText("");
                        handler.removeCallbacks(input_finish_checker);
                    }
                }
            }
        };

        gravityBarcodeEditText.addTextChangedListener(new TextWatcher() {
            boolean isIgnore = false;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacks(input_finish_checker);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() > 0) {
                    last_text_edit = System.currentTimeMillis();
                    handler.postDelayed(input_finish_checker, delay);
                }
//                String codeScanned = editable.toString();
//                if(editable.toString().length()<15)
//                    isGravityNeedToWatch = false;
//                else
//                    isGravityNeedToWatch = true;
//                if(!isIgnore) {
//                    if (isGravityNeedToWatch) {
//                        if (validateGravityPartCode(codeScanned)) {
//                            partBarcodeEditText.requestFocus();
//                            isIgnore = true;
//                            gravityBarcodeEditText.setText(codeScanned.substring(
//                                    0,
//                                    codeScanned.length()-1));
//                        }
//                        else {
//                            isIgnore = true;
//                            gravityBarcodeEditText.requestFocus();
//                            gravityBarcodeEditText.setText("");
//                            isIgnore = false;
//                        }
//                    }
//                }
            }
        });

        partBarcodeEditText.addTextChangedListener(new TextWatcher() {
            boolean isIgnore = false;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacks(input_finish_checker);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() > 0) {
                    last_text_edit = System.currentTimeMillis();
                    handler.postDelayed(input_finish_checker, delay);
                }
            }
        });

        LinearLayout restartButton;
        restartButton = (LinearLayout) findViewById(R.id.restart_button);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                okImage.setVisibility(GONE);
                notOkImage.setVisibility(GONE);
                partPosition.setVisibility(GONE);
                barcodeImage.setVisibility(VISIBLE);
                resetRoutine();
            }
        });

    }

    private boolean validateGravityPartCode(String barcode) {
        char lastChar = barcode.charAt(barcode.length() - 1);
        if(lastChar != '-') {
            Toast.makeText(this, R.string.gravity_barcode_error, LENGTH_SHORT).show();
            return false;
        }
        else {
            return true;
        }
    }

    private boolean validatePartCode(String barcode) {
        char lastChar = barcode.charAt(barcode.length() - 1);
        activeHarnessPosImage = checkHarnessPos(barcode);
        if(barcode.length() > 1 && lastChar == '-') {
            Toast.makeText(this, R.string.part_barcode_error, LENGTH_SHORT).show();
            return false;
        }
        else if(activeHarnessPosImage == null && errorState.equals(ERROR404)) {
            barcodeImage.setVisibility(GONE);
            partPosition.setVisibility(VISIBLE);
            partPosition.setBackgroundResource(R.drawable.error404);
            activeHarnessPosImage = partPosition;
            return false;
        }
        else if(activeHarnessPosImage == null && errorState.equals(ERRORIMAGE)) {
            barcodeImage.setVisibility(GONE);
            partPosition.setVisibility(VISIBLE);
            partPosition.setBackgroundResource(R.drawable.image_not_found_error);
            activeHarnessPosImage = partPosition;
            errorState = ERROR404;
            return false;
        }
        else {
            barcodeImage.setVisibility(GONE);
            partPosition.setVisibility(VISIBLE);
            return true;
        }
    }

    private ImageView checkHarnessPos(String barcode) {
        ImageView auxPartPosition = null;
        int imageToShow;
        for(int i=0; i<gravityParts.length; i++) {
            if(barcode.equals(gravityParts[i]) & !barcode.equals("0")) {
                imageToShow = getResId(matrix[i], R.drawable.class);
                if(imageToShow >= 0) {
                    partPosition.setBackgroundResource(imageToShow);
                    auxPartPosition = partPosition;
                }
                else
                    errorState = ERRORIMAGE;
            }
        }
        return auxPartPosition;
    }

    private void checkCompatibility(ImageView activeHarnessPosImage) {
        String gravityBarcode = gravityBarcodeEditText.getText().toString().trim();
        String partBarcode = partBarcodeEditText.getText().toString().trim();
        activeHarnessPosImage.setVisibility(GONE);
        if(gravityBarcode.equals(partBarcode)) {
            isResultOk = true;
            okImage.setVisibility(VISIBLE);
        }
        else {
            isResultOk = false;
            notOkImage.setVisibility(VISIBLE);
        }
        insertBarcodes(gravityBarcode, partBarcode, isResultOk);
    }

    private void insertBarcodes(String gravityBarcode, String partBarcode, boolean isResultOk) {
        StringBuilder initialText = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        String strDate = mdformat.format(calendar.getTime());

        try{
            File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File file = new File(dcim, "History.txt");

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null)
                initialText.append(line);
            br.close();
        }catch (IOException e) {
            e.printStackTrace();
        }

        try{
            File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File file = new File(dcim, "History.txt");

            FileWriter writer = new FileWriter(file);

            writer.append(initialText.toString());
            writer.flush();
            writer.append('\n');
            writer.flush();
            writer.append(partBarcode);
            writer.flush();
            writer.append(";");
            writer.flush();
            writer.append(gravityBarcode);
            writer.flush();
            writer.append(";");
            writer.flush();
            writer.append(String.valueOf(isResultOk));
            writer.flush();
            writer.append(";");
            writer.flush();
            writer.append(strDate);
            writer.flush();
            writer.append("\r\n");
            writer.flush();
            writer.close();
            //Toast.makeText(this, "Saved to History.txt", LENGTH_SHORT).show();
        } catch(IOException e) {
            Toast.makeText(this, "Problem saving history to DCIM", LENGTH_SHORT).show();
          e.printStackTrace();
        }

        int status;

        if(isResultOk)
            status = MATCH;
        else
            status = NOMATCH;

        ContentValues values = new ContentValues();
        values.put(BarcodeContract.BarcodeEntity.COLUMN_CODEBAR_GRAVITY, gravityBarcode);
        values.put(BarcodeContract.BarcodeEntity.COLUMN_CODEBAR_PART, partBarcode);
        values.put(BarcodeContract.BarcodeEntity.COLUMN_CODEBAR_STATUS, status);

        Uri newUri = getContentResolver().insert(BarcodeContract.BarcodeEntity.CONTENT_URI, values);

        if(newUri == null)
            Toast.makeText(this, "Error al salvar en historial", LENGTH_SHORT).show();
        //else
        //   Toast.makeText(this, "Salvado a historial", LENGTH_SHORT).show();
    }

    private static int getResId(String resName, Class <?> c) {
        try{
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch(Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    private void resetRoutine() {
        isGravityNeedToWatch = false;
        isPartNeedToWatch = false;
        gravityBarcodeEditText.setText("");
        partBarcodeEditText.setText("");
        isPartNeedToWatch = true;
        isResultOk = true;
        partBarcodeEditText.setBackgroundResource(R.drawable.barcode_number_text_view_background);
        gravityBarcodeEditText.setBackgroundResource(R.drawable.barcode_number_text_view_grey_background);
        partBarcodeEditText.setFocusable(true);
        partBarcodeEditText.setFocusableInTouchMode(true);
        partBarcodeEditText.requestFocus();
    }
}
