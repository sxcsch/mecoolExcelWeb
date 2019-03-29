package com.mecool.entity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Administrator on 2018/8/27.
 */
public class Application {

    private Properties properties;

    private String excelThreadNum = "";
    private String excelThreadOpen = "";

    public Application(){
        init();
    }
    public void init(){
        properties =  new Properties();
        try {
            InputStream in = Application.class.getClassLoader().getResourceAsStream("application.properties");
            properties.load(in);
            in.close();
            excelThreadNum = properties.getProperty("excelThreadNum");
            excelThreadOpen = properties.getProperty("excelThreadOpen");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getExcelThreadNum() {
        return Integer.parseInt(excelThreadNum);
    }

    public int getExcelThreadOpen() {
        return Integer.parseInt(excelThreadOpen);
    }
}
