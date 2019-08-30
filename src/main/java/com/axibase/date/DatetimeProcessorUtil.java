package com.axibase.date;

import java.time.*;

@SuppressWarnings("squid:S109") // magic constant
public final class DatetimeProcessorUtil {
    static final int NANOS_IN_MILLIS = 1_000_000;
    static final int MILLISECONDS_IN_SECOND = 1000;
    static final int UNIX_EPOCH_YEAR = 1970;
    static final int MAX_YEAR = 2200;
    private static final int ISO_LENGTH = "1970-01-01T00:00:00.000000000+00:00".length();
    private static final int TIVOLI_LENGTH = "1yyMMddHHmmssSSS".length();
    private static final int TIVOLI_EPOCH_YEAR = 1900;

    private DatetimeProcessorUtil() {}

    /**
     * Optimized print of a timestamp in ISO8601 or local format: yyyy-MM-dd[T| ]HH:mm:ss[.SSS]
     * @param timestamp milliseconds since epoch
     * @param offsetType Zone offset format: ISO (+HH:mm), RFC (+HHmm), or NONE
     * @return String representation of the timestamp
     */
    static String printIso8601(long timestamp, char delimiter, ZoneId zone, ZoneOffsetType offsetType, int fractionsOfSecond) {
        final StringBuilder sb = new StringBuilder(ISO_LENGTH);
        final OffsetDateTime dateTime;
        if (ZoneOffset.UTC.equals(zone)) {
            final long secs = Math.floorDiv(timestamp, MILLISECONDS_IN_SECOND);
            final int nanos = (int)Math.floorMod(timestamp, MILLISECONDS_IN_SECOND) * NANOS_IN_MILLIS;
            dateTime =  OffsetDateTime.of(LocalDateTime.ofEpochSecond(secs, nanos, ZoneOffset.UTC), ZoneOffset.UTC);
        } else {
            dateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zone);
        }
        adjustPossiblyNegative(sb, dateTime.getYear(), 4).append('-');
        adjust(sb, dateTime.getMonthValue(), 2).append('-');
        adjust(sb, dateTime.getDayOfMonth(), 2).append(delimiter);
        adjust(sb, dateTime.getHour(), 2).append(':');
        adjust(sb, dateTime.getMinute(), 2).append(':');
        adjust(sb, dateTime.getSecond(), 2);
        if (fractionsOfSecond > 0) {
            sb.append('.');
            adjust(sb, dateTime.getNano() / powerOfTen(9 - fractionsOfSecond), fractionsOfSecond);
        }
        return offsetType.appendOffset(sb, dateTime.getOffset()).toString();
    }

    static LocalDateTime parseTivoliDate(String date) {
        final int length = date.length();
        if (length != TIVOLI_LENGTH) {
            throw new IllegalArgumentException(date + " is not a valid Tivoli date: length must be " + TIVOLI_LENGTH);
        }
        return parseTivoliDate(date, length);
    }

    static ZonedDateTime parseTivoliDateWithOffset(String date) {
        if (date.length() > TIVOLI_LENGTH + 1) {
            final ZoneId zoneId = ZoneOffset.of(date.substring(TIVOLI_LENGTH + 1));
            return parseTivoliDate(date, TIVOLI_LENGTH).atZone(zoneId);
        } else {
            throw new IllegalArgumentException(date + " is not a valid Tivoli date with zone id");
        }
    }

    private static LocalDateTime parseTivoliDate(String date, int length) {
        int offset = 0;
        final int centuriesSinceEpoch = parseInt(date, offset, offset += 1, length);
        final int year = parseInt(date, offset, offset += 2, length);
        final int month = parseInt(date, offset, offset += 2, length);
        final int day = parseInt(date, offset, offset += 2, length);
        final int hour = parseInt(date, offset, offset += 2, length);
        final int minutes = parseInt(date, offset, offset += 2, length);
        final int seconds = parseInt(date, offset, offset += 2, length);
        final int millis = parseInt(date, offset, offset += 3, length);
        final int fullYear = TIVOLI_EPOCH_YEAR + centuriesSinceEpoch * 100 + year;
        final int nanos = millis * NANOS_IN_MILLIS;
        return LocalDateTime.of(fullYear, month, day, hour, minutes, seconds, nanos);
    }

    static String printTivoliDate(ZonedDateTime dateTime) {
        final StringBuilder sb = new StringBuilder(TIVOLI_LENGTH);
        final int century = (dateTime.getYear() - TIVOLI_EPOCH_YEAR) / 100;
        final int year = dateTime.getYear() % 100;
        adjustPossiblyNegative(sb, century, 1);
        adjust(sb, year, 2);
        adjust(sb, dateTime.getMonthValue(), 2);
        adjust(sb, dateTime.getDayOfMonth(), 2);
        adjust(sb, dateTime.getHour(), 2);
        adjust(sb, dateTime.getMinute(), 2);
        adjust(sb, dateTime.getSecond(), 2);
        adjust(sb, dateTime.getNano() / NANOS_IN_MILLIS, 3);
        return sb.toString();
    }

    static boolean checkExpectedMilliseconds(String date, int expected) {
        final int indexOfDot = date.indexOf('.');
        if (expected <= 0) {
            return indexOfDot < 0;
        } else {
            final int length = date.length();
            int cnt = 0;
            for (int i = indexOfDot + 1; i < length; i++) {
                if (Character.isDigit(date.charAt(i))) {
                    ++cnt;
                } else {
                    break;
                }
            }
            return cnt == expected;
        }
    }

    static ZonedDateTime parseIso8601AsZonedDateTime(String date, char delimiter, ZoneId defaultOffset, ZoneOffsetType offsetType) {
        try {
            final int length = date.length();

            final ParsingContext context = new ParsingContext();
            final LocalDateTime localDateTime = parseIso8601AsLocalDateTime(date, delimiter, context);
            int offset = context.offset;
            // extract timezone
            final ZoneId zoneId;
            if (offset == length) {
                if (offsetType != ZoneOffsetType.NONE || defaultOffset == null) {
                    throw new IllegalStateException("Zone offset required");
                }
                zoneId = defaultOffset;
            } else {
                if (offsetType == ZoneOffsetType.NONE) {
                    throw new IllegalStateException("Zone offset unexpected");
                }
                final char timezoneIndicator = date.charAt(offset);
                if (timezoneIndicator == 'Z' && offset == length - 1) {
                    zoneId = ZoneOffset.UTC;
                } else {
                    zoneId = ZoneOffset.of(date.substring(offset));
                }
            }
            return ZonedDateTime.of(localDateTime, zoneId);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Failed to parse date " + date, e);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid date " + date, e);
        }
    }

    private static int parseNanos(int value, int digits) {
        return value * powerOfTen(9 - digits);
    }

    private static int parseInt(String value, int beginIndex, int endIndex, int valueLength) throws NumberFormatException {
        if (beginIndex < 0 || endIndex > valueLength || beginIndex >= endIndex) {
            throw new NumberFormatException(value);
        }
        int result = resolveDigitByCode(value.charAt(beginIndex));
        for (int i = beginIndex + 1; i < endIndex; ++i) {
            result = result * 10 + resolveDigitByCode(value.charAt(i));
        }
        return result;
    }

    private static int resolveDigitByCode(char c) {
        final int result = c - '0';
        if (result < 0 || result > 9) {
            throw new NumberFormatException("Invalid digit: " + c);
        }
        return result;
    }

    private static void checkOffset(String value, int offset, char expected) throws IndexOutOfBoundsException {
        char found = value.charAt(offset);
        if (found != expected) {
            throw new IndexOutOfBoundsException("Expected '" + expected + "' character but found '" + found + "'");
        }
    }

    private static LocalDateTime parseIso8601AsLocalDateTime(String date, char delimiter, ParsingContext context) {
        final int length = date.length();
        int offset = context.offset;

        // extract year
        int year = parseInt(date, offset, offset += 4, length);
        checkOffset(date, offset, '-');

        // extract month
        int month = parseInt(date, offset += 1, offset += 2, length);
        checkOffset(date, offset, '-');

        // extract day
        int day = parseInt(date, offset += 1, offset += 2, length);
        checkOffset(date, offset, delimiter);

        // extract hours, minutes, seconds and milliseconds
        int hour = parseInt(date, offset += 1, offset += 2, length);
        checkOffset(date, offset, ':');

        int minutes = parseInt(date, offset += 1, offset += 2, length);
        checkOffset(date, offset, ':');

        int seconds = parseInt(date, offset += 1, offset += 2, length);
        // milliseconds can be optional in the format
        final int nanos;
        if (offset < length && date.charAt(offset) == '.') {
            final int startPos = ++offset;
            final int endPosExcl = Math.min(offset + 9, length);
            int frac = resolveDigitByCode(date.charAt(offset++));
            while (offset < endPosExcl) {
                final int digit = date.charAt(offset) - '0';
                if (digit < 0 || digit > 9) {
                    break;
                }
                frac = frac * 10 + digit;
                ++offset;
            }
            nanos = parseNanos(frac, offset - startPos);
        } else {
            nanos = 0;
        }
        context.offset = offset;
        return LocalDateTime.of(year, month, day, hour, minutes, seconds, nanos);
    }

    public static OffsetDateTime parseIso8601AsOffsetDateTime(String date, char delimiter) {
        try {
            final int length = date.length();
            final ParsingContext parsingContext = new ParsingContext();
            final LocalDateTime localDateTime = parseIso8601AsLocalDateTime(date, delimiter, parsingContext);
            int offset = parsingContext.offset;

            // extract timezone
            final ZoneOffset zoneOffset;
            if (offset == length) {
                throw new IllegalStateException("Zone offset required");
            } else {
                final char timezoneIndicator = date.charAt(offset);
                if (timezoneIndicator == 'Z' && offset == length - 1) {
                    zoneOffset = ZoneOffset.UTC;
                } else {
                    zoneOffset = ZoneOffset.of(date.substring(offset));
                }
            }
            return OffsetDateTime.of(localDateTime, zoneOffset);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Failed to parse date " + date, e);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid date " + date, e);
        }
    }

    public static StringBuilder appendformattedSecondOffset(int offsetSeconds, StringBuilder sb) {
        if (offsetSeconds == 0) {
            return sb.append('Z');
        }
        sb.append(offsetSeconds < 0 ? '-' : '+');
        final int absSeconds = Math.abs(offsetSeconds);
        adjust(sb, absSeconds / 3600, 2);
        adjust(sb, (absSeconds / 60) % 60, 2);
        return sb;
    }

    /**
     * Return number of digits in base-10 string representation.
     * @param number Non-negative number
     * @return number of digits
     */
    public static int sizeInDigits(int number) {
        if (number < 100_000) {
            if (number < 100) {
                return number < 10 ? 1 : 2;
            } else {
                if (number < 1000) {
                    return 3;
                } else {
                    return number < 10_000 ? 4 : 5;
                }
            }
        } else {
            if (number < 10_000_000) {
                return number < 1_000_000 ? 6 : 7;
            } else {
                if (number < 100_000_000) {
                    return 8;
                } else {
                    return number < 1_000_000_000 ? 9 : 10;
                }
            }
        }

    }

    public static int powerOfTen(int pow) {
        switch (pow) {
            case 0: return 1;
            case 1: return 10;
            case 2: return 100;
            case 3: return 1_000;
            case 4: return 10_000;
            case 5: return 100_000;
            case 6: return 1_000_000;
            case 7: return 10_000_000;
            case 8: return 100_000_000;
            case 9: return 1_000_000_000;
        }
        for (int accum = 1, b = 10;; pow >>= 1) {
            if (pow == 1) {
                return b * accum;
            } else {
                accum *= ((pow & 1) == 0) ? 1 : b;
                b *= b;
            }
        }
    }

    public static StringBuilder adjustPossiblyNegative(StringBuilder sb, int num, int positions) {
        if (num >= 0) {
            return adjust(sb, num, positions);
        }
        return adjust(sb.append('-'), -num, positions - 1);

    }

    public static StringBuilder adjust(StringBuilder sb, int num, int positions) {
        for (int i = positions - sizeInDigits(num); i > 0; --i) {
            sb.append('0');
        }
        return sb.append(num);
    }

    public static ZonedDateTime timestampToZonedDateTime(long timestamp, ZoneId zoneId) {
        return Instant.ofEpochMilli(timestamp).atZone(zoneId);
    }

    public static long toMillis(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant().toEpochMilli();
    }

    public static boolean isNumeric(final CharSequence cs) {
        if (cs == null) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Checks whether the String a valid Java number.</p>
     *
     * <p>Valid numbers include hexadecimal marked with the <code>0x</code> or
     * <code>0X</code> qualifier, octal numbers, scientific notation and
     * numbers marked with a type qualifier (e.g. 123L).</p>
     *
     * <p>Non-hexadecimal strings beginning with a leading zero are
     * treated as octal values. Thus the string <code>09</code> will return
     * <code>false</code>, since <code>9</code> is not a valid octal value.
     * However, numbers beginning with {@code 0.} are treated as decimal.</p>
     *
     * <p><code>null</code> and empty/blank {@code String} will return
     * <code>false</code>.</p>
     *
     * @param str  the <code>String</code> to check
     * @return <code>true</code> if the string is a correctly formatted number
     */
    public static boolean isCreatable(final String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        final char[] chars = str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        final int start = chars[0] == '-' || chars[0] == '+' ? 1 : 0;
        if (sz > start + 1 && chars[start] == '0') { // leading 0
            if (chars[start + 1] == 'x' || chars[start + 1] == 'X') { // leading 0x/0X
                int i = start + 2;
                if (i == sz) {
                    return false; // str == "0x"
                }
                // checking hex (it can't be anything else)
                for (; i < chars.length; i++) {
                    if ((chars[i] < '0' || chars[i] > '9')
                            && (chars[i] < 'a' || chars[i] > 'f')
                            && (chars[i] < 'A' || chars[i] > 'F')) {
                        return false;
                    }
                }
                return true;
            } else if (Character.isDigit(chars[start + 1])) {
                // leading 0, but not hex, must be octal
                int i = start + 1;
                for (; i < chars.length; i++) {
                    if (chars[i] < '0' || chars[i] > '7') {
                        return false;
                    }
                }
                return true;
            }
        }
        sz--; // don't want to loop to the last char, check it afterwords
        // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || i < sz + 1 && allowSigns && !foundDigit) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                hasDecPoint = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == '+' || chars[i] == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                // single trailing decimal point after non-exponent is ok
                return foundDigit;
            }
            if (!allowSigns
                    && (chars[i] == 'd'
                    || chars[i] == 'D'
                    || chars[i] == 'f'
                    || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l'
                    || chars[i] == 'L') {
                // not allowing L with an exponent or decimal point
                return foundDigit && !hasExp && !hasDecPoint;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }

    private static final class ParsingContext {
        private int offset;
    }
}
