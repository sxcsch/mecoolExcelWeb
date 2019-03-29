package com.mecool.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.mecool")
public class ExcelApplication {
    public static void main(String[] args) {
            SpringApplication.run(ExcelApplication.class, args);
    }
}
