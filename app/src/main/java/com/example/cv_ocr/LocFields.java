package com.example.cv_ocr;

import com.google.android.gms.vision.text.Line;

import java.util.ArrayList;

public class LocFields {
    ArrayList<Line> lines;
    ArrayList<ArrayList<String>> InfoFields;

    public void init(ArrayList<Line> lines)
        {
            this.lines = lines;
            this.sortLines();
        }
    public void inputFields()
        {
            //Input fields based on location
        }
    private void sortLines()
        {
            for(Line l : lines)
                {

                }
        }

}
