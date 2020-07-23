package com.example.cv_ocr;

/*
 * Helper class for string functions and indexing.
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

    public String rmnonAlphaNum(String toClean)
        {
            return toClean.replaceAll("[^a-zA-Z0-9]", "");
        }

    public int indexOfNum(String check, int numOccurence)
        {
            int occurence = 0;
            for(int i = 0 ; i < check.length() ; ++i)
                {
                    if(check.codePointAt(i)>=48 && check.codePointAt(i)<=57)occurence++;
                    if(occurence==numOccurence)return i;
                }
            return 0;
        }
}
