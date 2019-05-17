package com.example.android.barcodematcher;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GravityAddition extends AppCompatActivity {

    private static final String GRAVITYLINEUPFILE = "GravityLineUp.txt";
    private String textFromFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gravity_addition);

        BarcodeMatcherUtils utils = new BarcodeMatcherUtils();
        textFromFile = utils.readFromFile(GRAVITYLINEUPFILE);
    }
}
