package com.example.cv_ocr;



import android.util.Log;

import com.google.mlkit.vision.text.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Trying out location based filtering
 * Members are :
 * 1. Lines - ArrayList of lines object gotten from ML OCR process
 * 2. InfoFields 2D ArrayList of String type object, meant to simulate placement of KTP information
 */
public class LocFields {
    List<Text.Line> lines;
    List<List<Text.Line>> InfoFields = new ArrayList<List<Text.Line>>();;
    final int DELTA = 25;
    StringUtil checks;
    String provinsi, kota, NIK, nama, ttgl, kelamin, alamat, agama, status ,perkawinan, pekerjaan, kewarganegaraan;
    public void init(List<Text.Line> lines)
        {
            this.lines = lines;
            this.sortLines();
        }
    public void debugPrint()
        {
//            Log.i("First debug","This is the first debug");
//            for(int i = 0 ; i < lines.size() ; ++i)
//                {
//                    String print = "";
//                    print+=lines.get(i).getText()+" , "+lines.get(i).getCornerPoints()[0].y;
//                    Log.i("Line number "+String.valueOf(i),print);
//                }

            for(int i = 0 ; i < InfoFields.size() ; ++i)
                {
                    List<Text.Line> al = InfoFields.get(i);
                    String print = "";
                    for(int j = 0 ; j < al.size() ; ++j)
                        {
                            print +=al.get(j).getText()+"\t";
                        }
                    Log.i("Line "+String.valueOf(i),print);
                }
        }
    public void inputFields()
        {
            //Input fields based on location
            provinsi        = InfoFields.get(0).get(0).getText().substring(9).trim();
            kota            = InfoFields.get(1).get(0).getText().substring(5).trim();
            NIK             = InfoFields.get(2).get(InfoFields.get(2).size()-1).getText().trim();
            nama            = InfoFields.get(3).get(InfoFields.get(3).size()-1).getText().trim();
            ttgl            = InfoFields.get(4).get(InfoFields.get(4).size()-1).getText().trim();
            kelamin         = InfoFields.get(5).get(InfoFields.get(5).size()-2).getText().trim();
            alamat          = InfoFields.get(6).get(InfoFields.get(6).size()-1).getText().trim()+" "+
                              InfoFields.get(7).get(InfoFields.get(7).size()-1).getText().trim()+" "+
                              InfoFields.get(8).get(InfoFields.get(8).size()-1).getText().trim()+" "+
                              InfoFields.get(9).get(InfoFields.get(9).size()-1).getText().trim()+" "+
                              InfoFields.get(10).get(InfoFields.get(10).size()-1).getText().trim();
            agama           = InfoFields.get(11).get(InfoFields.get(11).size()-1).getText().trim();
            perkawinan      = InfoFields.get(12).get(InfoFields.get(12).size()-1).getText().trim();
            pekerjaan       = InfoFields.get(13).get(InfoFields.get(13).size()-1).getText().trim();
            kewarganegaraan = InfoFields.get(13).get(InfoFields.get(14).size()-1).getText().trim();
        }

    private void sortLines()
        {
            Collections.sort(lines,new SortByY());
            List<Text.Line> currRow = new ArrayList<Text.Line>();
            int currY = 0;
            for(int i = 0 ; i < lines.size() ; ++i)
                {
                    Text.Line currLine = lines.get(i);
                    if(Math.abs(currY-currLine.getCornerPoints()[0].y)>DELTA)
                        {
                            Collections.sort(currRow,new SortByX());
                            InfoFields.add(currRow);
                            currRow = new ArrayList<Text.Line>();
                            currRow.add(currLine);
                            currY = currLine.getCornerPoints()[0].y;
                        }
                    else
                        {
                            currRow.add(currLine);
                            currY = currLine.getCornerPoints()[0].y;
                        }

                }
        }




        //Sorts Line items by height
    class SortByY implements Comparator<Text.Line>
    {
        public int compare(Text.Line a, Text.Line b)
        {
            return a.getCornerPoints()[0].y - b.getCornerPoints()[0].y;
        }
    }
    class SortByX implements Comparator<Text.Line>
    {
        public int compare(Text.Line a, Text.Line b)
            {
                return a.getCornerPoints()[0].x - b.getCornerPoints()[0].x;
            }
    }
}
