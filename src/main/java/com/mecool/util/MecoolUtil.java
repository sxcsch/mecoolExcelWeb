package com.mecool.util;

import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MecoolUtil {

    /**
     * Parse days string, daysStr format like "2014/06/10,2014/06/13;2014/06/15；2014/06/20" or "014-06-25;2014-06-20", split by ",", ";","，","；".
     * @param daysStr
     * @return Map<String, Date>, key format is: "yyyy/MM/dd".
     */
    public static Map<String, Date> parseDaysStr(String daysStr) {
        // Parse Dates
        Map<String, Date> ssm = new HashMap<String, Date>();
        String[] ss1 = daysStr.split(",");
        for (int i = 0; i < ss1.length; i++) {
            String s1 = ss1[i];
            if (StringUtils.isBlank(s1) || s1.length() < 6) {
                continue;
            }
            String[] ss2 = s1.split(";");
            for (int j = 0; j < ss2.length; j++) {
                String s2 = ss2[j];
                if (StringUtils.isBlank(s2) || s2.length() < 6) {
                    continue;
                }
                String[] ss3 = s2.split("，");
                for (int m = 0; m < ss3.length; m++) {
                    String s3 = ss3[m];
                    if (StringUtils.isBlank(s3) || s3.length() < 6) {
                        continue;
                    }
                    String[] ss4 = s3.split("；");
                    for (int n = 0; n < ss4.length; n++) {
                        String s4 = ss4[n];
                        if (StringUtils.isBlank(s4) || s4.length() < 6) {
                            continue;
                        }
                        s4 = s4.trim();
                        if (ssm.get(s4) != null) {
                            continue;
                        }
                        try {
                            ssm.put(s4, ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(s4));
                        } catch (ParseException e) {
                            try {
                                Date d = ConstantsMecool.SIMPLE_DATE_FORMAT.parse(s4);
                                ssm.put(ConstantsMecool.SIMPLE_DATE_FORMAT1.format(d), d);
                            } catch (ParseException e1) {
                                //
                            }
                        }
                    }
                }
            }
        }
        return ssm;
    }

    public static Date getDateNoTime(Date d) {
        if (d == null) {
            return null;
        }
        Date v = d;
        String todayStr = ConstantsMecool.SIMPLE_DATE_FORMAT.format(d);
        try {
            v = ConstantsMecool.SIMPLE_DATE_FORMAT.parse(todayStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return v;
    }
}
