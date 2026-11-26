package lakbaylokal.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static String formatDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat(DATE_PATTERN).format(date);
    }

    public static String formatDateTime(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat(DATETIME_PATTERN).format(date);
    }

    public static String nowDateTime() {
        return formatDateTime(new Date());
    }

    public static String nowDate() {
        return formatDate(new Date());
    }
}
