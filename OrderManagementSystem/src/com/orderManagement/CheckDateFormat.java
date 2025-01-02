package com.orderManagement;

public class CheckDateFormat {
    public static boolean isValidDate(String date) {
        //查看日期格式是否只含有数字和'-'
        for (int i = 0; i < date.length(); i++) {
            char ch = date.charAt(i);
            if (!Character.isDigit(ch) && ch!='-') {
                return false;
            }
        }
        //查看日期是否含有3组数字，且年月日均在有效范围内
        String[] dateArr = date.split("-");
        if (dateArr.length != 3)
            return false;

        double year = Double.parseDouble(dateArr[0]);
        double month = Double.parseDouble(dateArr[1]);
        double day = Double.parseDouble(dateArr[2]);

        int year_int=Integer.parseInt(dateArr[0]);
        int month_int=Integer.parseInt(dateArr[1]);
        int day_int=Integer.parseInt(dateArr[2]);

        //判断年月日是否是浮点数
        if(year-year_int!=0 || month-month_int!=0 || day-day_int!=0)
            return false;
        //判断年月日是否在有效范围内
        if(year_int<1900 || year_int>2100 || month_int<1 || month_int>12 || day_int<1 || day_int>31)
            return false;
        return true;
    }
}
