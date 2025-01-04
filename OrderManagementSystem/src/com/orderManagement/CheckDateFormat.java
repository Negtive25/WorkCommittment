package com.orderManagement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

//查看日期格式是否正确
public class CheckDateFormat {
    public static boolean isValidDate(String date) {
        try {
            //创建一个日期格式化器，用于解析日期字符串
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            //使用日期格式化器解析日期字符串
            LocalDate.parse(date, formatter);
            //如果解析成功，返回true
            return true;
        } catch (DateTimeParseException e) {
            //如果解析失败，返回false
            return false;
        }
    }
}
