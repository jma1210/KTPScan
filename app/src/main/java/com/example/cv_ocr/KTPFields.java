package com.example.cv_ocr;

import android.util.Log;

import com.google.mlkit.vision.text.Text;

import java.util.List;

public class KTPFields {
    String[] infoFields = new String[5];
    boolean[] infoBools = new boolean[5];
    /*
    Nama = 0
    NIK = 1
    Provinsi = 2
    Kota = 3
    Tempat / Tanggal Lahir = 4
     */
    StringUtil checks = new StringUtil();
            public void inputFields(List<String> line)
                {
                    Log.i("Masuk sini","");
                    for(int i = 0 ; i < line.size() ; ++i)
                        {
                            String toCheck = line.get(i).trim();
                            if(checks.checkIfCaps(toCheck)&&infoBools[2]&&infoBools[3]&&!infoBools[0]&&toCheck.length()>3)
                                {
                                    infoFields[0] = toCheck;
                                    infoBools[0] = true;
                                    continue;
                                }
                            if(toCheck.contains("PROVINSI") && !infoBools[2])
                                {
                                    infoFields[2] = toCheck.substring(9);
                                    infoBools[2] = true;
                                    continue;
                                }
                            if(toCheck.contains("KOTA") && !infoBools[3])
                                {
                                    infoFields[3] = toCheck.substring(5);
                                    infoBools[3] = true;
                                    continue;
                                }
                        }
                    for(int i = 0; i < infoFields.length ; ++i)
                        {
                            if(infoBools[i])Log.i("Info number "+String.valueOf(i),infoFields[i]);
                        }
                };
}
