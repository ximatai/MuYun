package net.ximatai.muyun.database.tool;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTool {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Date stringToSqlDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty.");
        }

        try {
            LocalDate localDate = LocalDate.parse(dateString.substring(0, 10), DATE_FORMATTER);
            return Date.valueOf(localDate);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateString);
        }
    }

    public static Timestamp stringToSqlTimestamp(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        try {
            if (dateString.length() == 10) {
                dateString += " 00:00:00";
            }
            LocalDateTime localDateTime = LocalDateTime.parse(dateString, DATE_TIME_FORMATTER);
            return Timestamp.valueOf(localDateTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid datetime format: " + dateString);
        }
    }

    public static Timestamp handleDateTimestamp(Object value) {
        if (value instanceof Timestamp) {
            return (Timestamp) value;
        } else if ("".equals(value)) {
            return null;
        } else if (value instanceof LocalDateTime localDateTime) {
            return Timestamp.valueOf(localDateTime);
        } else if (value instanceof Date date) {
            return new Timestamp(date.getTime());
        } else if (value instanceof String str) {
            return stringToSqlTimestamp(str);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        }
    }
}
