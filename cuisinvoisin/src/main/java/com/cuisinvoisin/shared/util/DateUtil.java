// com/cuisinvoisin/shared/util/DateUtil.java
package com.cuisinvoisin.shared.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateUtil {
    private DateUtil() {}

    private static final ZoneId TUNISIA_ZONE = ZoneId.of("Africa/Tunis");
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(TUNISIA_ZONE);

    public static String format(Instant instant) {
        if (instant == null) return null;
        return DISPLAY_FORMATTER.format(instant);
    }

    public static Instant nowPlusSeconds(long seconds) {
        return Instant.now().plusSeconds(seconds);
    }
}
