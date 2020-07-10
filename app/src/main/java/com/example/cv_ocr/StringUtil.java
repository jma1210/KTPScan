package com.example.cv_ocr;
/*
Helper class for string functions.
 */
public class StringUtil {
    public boolean checkIfCaps(String check)
        {
            for(int i = 0 ; i < check.length() ; ++i)
                if(!(check.codePointAt(i)>=65 && check.codePointAt(i)<=90)&&check.codePointAt(i)!=32)
                    return false;
            return true;
        }

    public boolean containSymbol(String check)
        {
            for(int i = 0 ; i < check.length() ; ++i)
            {
                int code = check.codePointAt(i);
                if ((code >= 33 && code <= 47) || (code >= 58 && code <= 64) || (code >= 91 && code <= 96) || (code >= 123 && code <=126))
                    return true;
            }
            return false;
        }
    public boolean containsNum(String check)
        {
            boolean returnable = false;
            for(int i = 0 ; i<check.length() ; ++i)
                if(check.codePointAt(i)>=48 && check.codePointAt(i)<=57)
                {
                    returnable = true;
                    break;
                }
            return returnable;
        }
    public int nthLastIndex(int nth, String ch, String string)
        {
            if(nth <= 0) return string.length();
            return nthLastIndex(--nth,ch,string.substring(0,string.lastIndexOf(ch)));
        }
    public String rmnonAlphaNum(String toClean)
        {
            return toClean.replaceAll("[^a-zA-Z0-9]", "");
        }

}
