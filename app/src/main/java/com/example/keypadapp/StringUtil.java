package com.example.keypadapp;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringUtil {

    private StringUtil() {}

    /**
     * 메소드 : null 체크
     * @param str
     * @return : str
     */
    public static String nvl(String str) {
        return nvl(str, "");
    }
    public static String nvl(Object str) {
        return nvl((String) str, "");
    }

    public static String nvl(Object str, String defaultValue) {
        return nvl((String) str, defaultValue);
    }

    /**
     * 메소드 : String 변수가 null 경우 기본값 셋팅
     * @param str
     * @param defaultValue
     * @return : rtnValue
     */
    public static String nvl(String str, String defaultValue) {
        String rtnValue = "";
        if(str == null || str.length() == 0) {
            rtnValue = defaultValue;
        } else {
            rtnValue = str;
        }
        return rtnValue;
    }

}
