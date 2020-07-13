package com.example.cv_ocr;

import com.google.android.gms.vision.text.Line;

import java.util.ArrayList;

/**
 * Trying out location based filtering
 * Members are :
 * 1. Lines - ArrayList of lines object gotten from ML OCR process
 * 2. InfoFields 2D ArrayList of String type object, meant to simulate placement of KTP information
 */
public class LocFields {
    ArrayList<Line> lines;
    ArrayList<ArrayList<String>> InfoFields;
    final int DELTA = 25;
    StringUtil checks;

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
            int currY = 0;
            int row =  0;
            for(int i = 0 ; i < lines.size() ; ++i)
                {
                    Line check = lines.get(i);
                        if(Math.abs(currY-check.getCornerPoints()[0].y)<DELTA && currY>0)
                            {
                                currY = check.getCornerPoints()[0].y;
                                InfoFields.get(row).add(check.toString().trim());
                            }
                        else
                            {
                                currY = check.getCornerPoints()[0].y;
                                InfoFields.add(new ArrayList<String>());
                                InfoFields.get(row++).add(check.toString().trim());
                            }

                }
        }

}
