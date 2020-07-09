package com.example.cv_ocr;

import android.util.Log;

import com.google.mlkit.vision.text.Text;

import java.util.List;

public class KTPFields {
    String provinsi,nama,kota,nik,tempattgl;
    StringUtil checks;
            public void inputFields(List<String> line)
                {
                    Log.i("Masuk sini","");
                    for(int i = 0 ; i < line.size() ; ++i)
                        {
                            Log.i("Line "+String.valueOf(i),line.get(i));
                        }
                };
}
