package com.crqs.command.cqrs_command.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }

}
