package com.example.android.barcodematcher;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GravityStarting extends AppCompatActivity {

    private EditText columnsNumberEditText;
    private EditText rowsNumberEditText;
    private TextView gravityPos;
    private EditText scannedCodebar;
    private LinearLayout gravitySchema;
    private LinearLayout buttonsLayout;
    private int currentPos = 0;
    private String[] matrixWithPos;
    private Map<String, String> valuesToWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gravity_starting);

        initValues();

    }

    private void initValues() {
        final Button saveButton;
        Button retryButton;

        columnsNumberEditText = (EditText) findViewById(R.id.survey_columns_qty_edit_text);
        rowsNumberEditText = (EditText) findViewById(R.id.survey_rows_qty_edit_text);
        gravityPos = (TextView) findViewById(R.id.survey_gravity_pos_text_view);
        scannedCodebar = (EditText) findViewById(R.id.survey_barcode_edit_text);
        gravitySchema = (LinearLayout) findViewById(R.id.survey_gravity_schema_linear_layout);
        buttonsLayout = (LinearLayout) findViewById(R.id.survey_buttons_linear_layout);
        saveButton = (Button) findViewById(R.id.survey_save_button);
        retryButton = (Button) findViewById(R.id.survey_retry_button);
        Switch readySwitch = (Switch) findViewById(R.id.survey_ready_switch);
        valuesToWrite = new HashMap<>();

        gravitySchema.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.GONE);
        if(scannedCodebar.getText().toString().equals("")) {
            saveButton.setEnabled(false);
        }

        /**
         * SaveButton will stay unfocusable until there is a codebar scanned.
         */
        scannedCodebar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                saveButton.setEnabled(true);
            }
        });

        /**
         * When retry button is pressed, scanned code bar text view is clear and it's ready
         * for a new scan.
         */
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scannedCodebar.setText("");
                saveButton.setEnabled(false);
            }
        });

        /**
         * When saved button is pressed, this method saved the current gravity pos scanned bar
         * and write it on the "GravityLineUp.txt" file.
         * Then shows the next position to be scanned.
         */
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                valuesToWrite.put(gravityPos.getText().toString(),
                        scannedCodebar.getText().toString());
                currentPos++;
                if(currentPos < matrixWithPos.length) {
                    gravityPos.setText(matrixWithPos[currentPos]);
                    scannedCodebar.setText("");
                    saveButton.setEnabled(false);
                }
                if(currentPos == matrixWithPos.length) {
                    saveButton.setEnabled(false);
                    writeOnTextFile(valuesToWrite);
                    Intent intent = new Intent();
                    intent.putExtra("activity", 1);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        /**
         * When the switch is ON, then the lowest layout will be shown and columns and rows qty
         * are saved to the HashMap List.
         */
        readySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                valuesToWrite.put("columns", columnsNumberEditText.getText().toString());
                valuesToWrite.put("rows", rowsNumberEditText.getText().toString());

                columnsNumberEditText.setEnabled(false);
                rowsNumberEditText.setEnabled(false);
                gravitySchema.setVisibility(View.VISIBLE);
                buttonsLayout.setVisibility(View.VISIBLE);
                decodeGravityPos(Integer.valueOf(columnsNumberEditText.getText().toString()),
                        Integer.valueOf(rowsNumberEditText.getText().toString()));
                gravityPos.setText(matrixWithPos[currentPos]);
            }
        });
    }

    private void decodeGravityPos(int columnsNumberValue, int rowsNumberValue) {
        int[] columnsArray = new int[columnsNumberValue];
        String[] rowsArray = new String[rowsNumberValue];
        matrixWithPos = new String[columnsNumberValue * rowsNumberValue];
        int auxMatrixIndex = 0;
        int auxOverflow = 0;
        int auxFirstLetter = 0;

        for (int r=0; r < rowsArray.length; r++) {
            rowsArray[r] = "" + (r + 1);
            for (int c=0; c < columnsArray.length; c++) {
                if(c <= 25) {
                    columnsArray[c] = (char) 65 + c;
                    matrixWithPos[auxMatrixIndex] = "" + (char) (columnsArray[c]) +
                            rowsArray[r];
                }
                else {
                    columnsArray[c] = (char) 65 +  auxFirstLetter;
                    matrixWithPos[auxMatrixIndex] = "" +
                            (char) (columnsArray[c]) +
                            (char) (65 + auxOverflow) +
                            rowsArray[r];
                    auxOverflow++;
                }
                auxMatrixIndex++;
                if((auxOverflow > 0) && (auxOverflow % 25 == 0))
                    auxFirstLetter++;
            }
            auxOverflow = 0;
        }
    }

    /**
     * This method will write the "GravityLineUp.txt" file if it exists.
     * If not or if it's empty, then will create it and fill it.
     */
    private void writeOnTextFile(Map<String, String> values) {
        String columns = values.get("columns");
        String rows = values.get("rows");

        if(Integer.valueOf(columns) < 10)
            columns = "0" + values.get("columns");
        if(Integer.valueOf(rows) < 10)
            rows = "0" + values.get("rows");

        try{
            File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File file = new File(dcim, "GravityLineUp.txt");

            FileWriter writer = new FileWriter(file);
            writer.write("");

            writer.append("Columnas = ");
            writer.flush();
            writer.append(columns);
            writer.flush();
            writer.append("\r\n");
            writer.flush();
            writer.append("Filas = ");
            writer.flush();
            writer.append(rows);
            writer.flush();
            writer.append("\r\n");
            writer.flush();

            for (String gravityPos : matrixWithPos) {
                writer.append(gravityPos);
                writer.flush();
                writer.append("\r\n");
                writer.flush();
                writer.append(values.get(gravityPos));
                writer.flush();
                writer.append("\r\n");
                writer.flush();
            }

            writer.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }


}
