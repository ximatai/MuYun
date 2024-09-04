package net.ximatai.muyun.core.tool;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTool {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Date stringToSqlDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty.");
        }
        dateString = dateString.substring(0, 10);
        LocalDate localDate = LocalDate.parse(dateString, DATE_FORMATTER);
        return Date.valueOf(localDate);
    }

    public static Date stringToSqlTime(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty.");
        }
        if (dateString.length() == 10) {
            dateString += " 00:00:00";
        }
        LocalDateTime localDateTime = LocalDateTime.parse(dateString, TIME_FORMATTER);
        return Date.valueOf(localDateTime.toLocalDate());
    }
}

