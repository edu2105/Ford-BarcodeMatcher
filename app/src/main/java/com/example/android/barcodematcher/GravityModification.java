package com.example.android.barcodematcher;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GravityModification extends AppCompatActivity {

    private static final String GRAVITYLINEUPFILE = "GravityLineUp.txt";
    private String textFromFile;
    private String[] decodePositions;
    private String[] gravityParts;
    private Button saveButton;
    private TextView oldCodeBarTextView;
    private EditText newCodeBarTextView;
    private TextView gravityPositionTextView;
    private BarcodeMatcherUtils utils;
    private int indexModified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gravity_modification);

        utils = new BarcodeMatcherUtils();
        textFromFile = utils.readFromFile(GRAVITYLINEUPFILE);
        gravityParts = utils.populateGravityArray(textFromFile).get(0);
        decodePositions = utils.populateGravityArray(textFromFile).get(1);

        initValues();
    }

    /**
     * Initi parameters, populate spinner and manage saveButton focus.
     */
    private void initValues() {
        saveButton = (Button) findViewById(R.id.mod_save_button);
        Button retryButton = (Button) findViewById(R.id.mod_retry_button);
        Spinner spinner = (Spinner) findViewById(R.id.mod_spinner);
        oldCodeBarTextView = (TextView) findViewById(R.id.mod_old_barcode_text_view);
        newCodeBarTextView = (EditText) findViewById(R.id.mod_new_barcode_edit_text);
        gravityPositionTextView = (TextView) findViewById(R.id.mod_gravity_pos_text_view);

        saveButton.setEnabled(false);

        for(int i=0; i<decodePositions.length; i++)
            decodePositions[i] = decodePositions[i].toUpperCase();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, decodePositions);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                indexModified = i;
                gravityPositionTextView.setText(decodePositions[i].toUpperCase());
                oldCodeBarTextView.setText(gravityParts[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        newCodeBarTextView.addTextChangedListener(new TextWatcher() {
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

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newCodeBar = newCodeBarTextView.getText().toString();
                updateFile(newCodeBar);
                textFromFile = utils.readFromFile(GRAVITYLINEUPFILE);
                gravityParts = utils.populateGravityArray(textFromFile).get(0);
                newCodeBarTextView.setText("");
                oldCodeBarTextView.setText(newCodeBar);
                saveButton.setEnabled(false);
            }
        });

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newCodeBarTextView.setText("");
                saveButton.setEnabled(false);
            }
        });
    }

    private void updateFile(String newCodeBarScanned) {
        int indexColumns = 11;
        int indexRows = textFromFile.indexOf("Filas") + 8;
        String columns = textFromFile.substring(indexColumns, indexColumns + 2);
        String rows = textFromFile.substring(indexRows, indexRows + 2);

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

            for (int i=0; i<decodePositions.length; i++) {
                writer.append(decodePositions[i]);
                writer.flush();
                writer.append("\r\n");
                writer.flush();
                if(i == indexModified)
                    writer.append(newCodeBarScanned);
                else
                    writer.append(gravityParts[i]);
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
