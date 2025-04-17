package org.com.code.im.utils;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.net.InetAddress;

public class GeoIpUtil {

    private static DatabaseReader reader;
    private static String FILE_PATH = "src/main/resources/GeoLite2-City.mmdb";

    static {
        try {
            File database = new File(FILE_PATH);
            if (!database.exists() || !database.isFile()) {
                throw new RuntimeException("数据库文件不存在");
            }
            reader = new DatabaseReader.Builder(database).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getIpLocation(String ip) {
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = reader.city(ipAddress);

            Country country = response.getCountry();
            City city = response.getCity();

            return country.getName() + " " + city.getName();
        } catch (Exception e) {
            e.printStackTrace();
            return "未知归属地";
        }
    }

    public static String getClientIpAddress(HttpServletRequest request) {
        /**
         * 优先从 X-Forwarded-For 头部获取真实 IP 地址
         */
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || ip.equalsIgnoreCase("unknown")) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || ip.equalsIgnoreCase("unknown")) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || ip.equalsIgnoreCase("unknown")) {
            ip = request.getRemoteAddr();
        }

        /**
         * 如果是多级代理，则取第一个非 unknown 的 IP
         */
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}