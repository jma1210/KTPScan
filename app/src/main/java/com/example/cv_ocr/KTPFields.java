package com.example.cv_ocr;

import android.util.Log;

import com.google.mlkit.vision.text.Text;

import java.util.List;

public class KTPFields {
    String[] infoFields = new String[9];
    boolean[] infoBools = new boolean[9];
    String toPrint = "";
    /*
    Nama = 0
    NIK = 1
    Provinsi = 2
    Kota = 3
    Tanggal Lahir = 4
    Tempat Lahir = 5
    Status kawin = 6
    Kewarganegaraan  = 7
    Kecamatan = 9
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
                            if(toCheck.contains("Status") && !infoBools[6])
                                {
                                    insertData(6, toCheck.substring(toCheck.length() - 11).trim());
                                    continue;
                                }
                            if(toCheck.contains("Kewarg") && !infoBools[7])
                                {
                                    insertData(7,toCheck.substring(toCheck.indexOf(" ",0)).trim());
                                    continue;
                                }
                            if(toCheck.contains("Kecam")&& !infoBools[8]) {
                                if (toCheck.indexOf(" ") == -1)
                                    insertData(8, toCheck);
                                else
                                    insertData(8, toCheck.substring(toCheck.indexOf(" ")).trim());
                                continue;
                            }
                        }
                    for(int i = 0 ; i<infoBools.length;++i)
                    {
                        if(infoBools[i])toPrint+=infoFields[i]+'\n';
                    }
                };

            public void insertData(int dataIndex, String inputData)
                {
                    infoFields[dataIndex] = inputData;
                    infoBools[dataIndex] = true;
                }
}
