package com.example.android.barcodematcher;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BarcodeMatcherUtils {

    BarcodeMatcherUtils() {}

    public String readFromFile(String fileName) {
        StringBuilder text = new StringBuilder();
        try{
            File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File file = new File(dcim, fileName);

            if(file.createNewFile())
                Log.e("BarcodeMatcherUtils", "File Created");
            else
                Log.e("BarcodeMatcherUtils", "File was not Created");

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

        return text.toString();
    }

    public List<String[]> populateGravityArray(String textFromFile) {
        List<String[]> finalDecode = new ArrayList<>();
        String[] gravityParts;
        String matrix[];
        int columns;
        int rows;
        int indexColumns = 11;
        int indexRows = textFromFile.indexOf("Filas") + 8;
        int[] matrixIndex;
        int auxMatrix = 0;
        int auxMatrixIndex = 0;

        columns = Integer.valueOf(textFromFile.substring(indexColumns, indexColumns + 2));
        rows = Integer.valueOf(textFromFile.substring(indexRows, indexRows + 2));
        int[] columnsName = new int [columns];
        String[] rowsName = new String [rows];

        int auxFirstLetter = 0;
        int auxOverflow = 0;

        matrix = new String[columns * rows];
        matrixIndex = new int[columns * rows];
        gravityParts = new String[columns * rows];


        for (int r=0; r < rows; r++) {
            rowsName[r] = "" + (r + 1);
            for (int c=0; c < columns; c++) {
                if(c <= 25) {
                    columnsName[c] = (char) 65 + c;
                    matrix[auxMatrix] = "" +
                            (char) (columnsName[c]) +
                            rowsName[r];
                    matrixIndex[auxMatrixIndex] = textFromFile.indexOf(
                            "*" + (char) columnsName[c] + rowsName[r] + "*");
                }
                else {
                    columnsName[c] = (char) 65 +  auxFirstLetter;
                    matrix[auxMatrix] = "" +
                            (char) (columnsName[c]) +
                            (char) (65 + auxOverflow) +
                            rowsName[r];
                    matrixIndex[auxMatrixIndex] = textFromFile.indexOf("*" +
                            (char) (columnsName[c]) +
                            (char) (65 + auxOverflow) +
                            rowsName[r]+
                            "*");
                    auxOverflow++;
                }
                auxMatrix++;
                auxMatrixIndex++;
                if((auxOverflow > 0) && (auxOverflow % 25 == 0))
                    auxFirstLetter++;
            }
            auxOverflow = 0;
        }

        for(int l=0; l<gravityParts.length - 1; l++) {
            gravityParts[l] = textFromFile.substring(matrixIndex[l] + 4, matrixIndex[l+1]);
        }

        gravityParts[gravityParts.length - 1] = textFromFile.substring(
                matrixIndex[matrixIndex.length - 1] + 4,
                textFromFile.length() - 1);

        finalDecode.add(gravityParts);
        finalDecode.add(matrix);

        return finalDecode;
    }
}
