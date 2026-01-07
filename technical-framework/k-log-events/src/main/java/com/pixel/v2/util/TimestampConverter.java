package com.pixel.v2.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility component for converting timestamp formats.
 */
public class TimestampConverter {

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    /**
     * Converts compact timestamp format yyyyMMddHHmmssSSS to ISO format yyyy-MM-ddTHH:mm:ss.SSSSSS
     * 
     * @param compactTimestamp the compact format timestamp (e.g., "20260107154119230")
     * @return ISO format timestamp (e.g., "2026-01-07T15:41:19.230000")
     */
    public static String convertToISO(String compactTimestamp) {
        if (compactTimestamp == null || compactTimestamp.length() != 17) {
            // Return current timestamp as fallback
            return LocalDateTime.now().format(ISO_FORMATTER);
        }

        try {
            String year = compactTimestamp.substring(0, 4);
            String month = compactTimestamp.substring(4, 6);
            String day = compactTimestamp.substring(6, 8);
            String hour = compactTimestamp.substring(8, 10);
            String minute = compactTimestamp.substring(10, 12);
            String second = compactTimestamp.substring(12, 14);
            String millis = compactTimestamp.substring(14, 17);

            LocalDateTime dateTime =
                    LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(month),
                            Integer.parseInt(day), Integer.parseInt(hour), Integer.parseInt(minute),
                            Integer.parseInt(second), Integer.parseInt(millis) * 1_000_000 // Convert
                                                                                           // millis
                                                                                           // to
                                                                                           // nanos
                    );

            return dateTime.format(ISO_FORMATTER);

        } catch (Exception e) {
            // Return current timestamp as fallback
            return LocalDateTime.now().format(ISO_FORMATTER);
        }
    }
}
