package com.example.cv_ocr;

import android.util.Log;

import com.google.mlkit.vision.text.Text;

import java.util.List;

public class KTPFields {
    String[] infoFields = new String[6];
    boolean[] infoBools = new boolean[6];
    /*
    Nama = 0
    NIK = 1
    Provinsi = 2
    Kota = 3
    Tanggal Lahir = 4
    Tempat Lahir = 5
     */
    StringUtil checks = new StringUtil();
            public void inputFields(List<String> line)
                {
                    Log.i("Masuk sini","");
                    for(int i = 0 ; i < line.size() ; ++i)
                        {
                            String toCheck = line.get(i).trim();
                            if(!infoBools[0] && infoBools[1]&&infoBools[2]&&infoBools[3]&&!checks.containSymbol(toCheck)&&!toCheck.contains("Nama"))
                                {
                                    insertData(0,toCheck);
                                    continue;
                                }
                            if(!infoBools[1] && checks.containsNum(toCheck))
                                {
                                    insertData(1,toCheck);
                                    continue;
                                }
                            if(toCheck.contains("PROVINSI") && !infoBools[2])
                                {
                                    insertData(2,toCheck.substring(9));
                                    continue;
                                }
                            if(toCheck.contains("KOTA") && !infoBools[3])
                                {
                                    insertData(3,toCheck.substring(5));
                                    continue;
                                }
                            if(toCheck.contains("-") && !infoBools[4])
                                {
                                    insertData(4,toCheck.substring(toCheck.length()-10));
                                    String mod = toCheck.substring(checks.nthLastIndex(2," ",toCheck),checks.nthLastIndex(1," ",toCheck));
                                    insertData(5, checks.rmnonAlphaNum(mod));
                                    continue;
                                }
                        }
                    for(int i = 0; i < infoFields.length ; ++i)
                        {
                            if(infoBools[i])Log.i("Info field ["+String.valueOf(i)+"]",infoFields[i]);
                        }
                };

            public void insertData(int dataIndex, String inputData)
                {
                    infoFields[dataIndex] = inputData;
                    infoBools[dataIndex] = true;
                }
}
