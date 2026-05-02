package com.seaman.utils;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.regex.Pattern;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class ObjectValidatorUtils {

    public static final String APPLICATION_VALUE_TYPE_INTEGER = "Integer";
    public static final String APPLICATION_VALUE_TYPE_FLOAT = "Float";
    public static final String APPLICATION_VALUE_TYPE_DOUBLE = "Double";
    public static final String APPLICATION_VALUE_TYPE_LONG = "Long";
    public static final String APPLICATION_VALUE_TYPE_BIGINTEGER = "BigInteger";
    public static final String APPLICATION_VALUE_TYPE_BOOLEAN = "Boolean";
    public static final String APPLICATION_VALUE_MOBILE_PATTERN = "^0([0-9])\\d{8}$";
    public static final String APPLICATION_VALUE_MOBILE_MASK_PATTERN = "^([x]{3}[- .]?){2}\\d{4}$";
    public static final String APPLICATION_VALUE_RGB_PATTERN = "#[0-9a-f]{6}";
    public static final String APPLICATION_VALUE_UUID = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";
    public static final String APPLICATION_VALUE_JWT = "^([a-zA-Z0-9_=]+)\\.([a-zA-Z0-9_=]+)\\.([a-zA-Z0-9_\\-\\+\\/=]*)";

    public static final String APPLICATION_NUMBER_PATTERN  = "^[0-9]{10}";
    public static final String APPLICATION_CHAR_PATTERN  = "^[A-Z]{2}";
    public static final String THAI_LANG_PATTERN = "^[ก-๏\\s]+$";

    public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

    public static final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD);

    public static boolean validateMandatory(String str) {
        return !("".equals(str.trim()) || str.trim().length() == 0 || "null".equalsIgnoreCase(str));
    }

    public static boolean validatePattern(String input, String regex) {
        if (isNotEmpty(regex)) {
            Pattern pattern = Pattern.compile(regex);
            return pattern.matcher(input).matches();
        }
        return true;
    }

    public static boolean validateLength(String input, int maxLength) {
        return input != null && (input.length() <= maxLength);
    }

    public static boolean validateFixLength(String input, int maxLength) {
        return input != null && (input.length() == maxLength);
    }

    public static boolean validateLengthEqual(String input, int maxLength) {
        return input != null && (input.length() == maxLength);
    }

    public static boolean validateType(String input, String validateType) {
        try {
            if (isNotEmpty(input)) {
                if (APPLICATION_VALUE_TYPE_INTEGER.equalsIgnoreCase(validateType)) {
                    Integer.parseInt(input);

                } else if (APPLICATION_VALUE_TYPE_FLOAT.equalsIgnoreCase(validateType)) {
                    Float.parseFloat(input);

                } else if (APPLICATION_VALUE_TYPE_DOUBLE.equalsIgnoreCase(validateType)) {
                    Double.parseDouble(input);

                } else if (APPLICATION_VALUE_TYPE_LONG.equalsIgnoreCase(validateType)) {
                    Long.parseLong(input);

                } else if (APPLICATION_VALUE_TYPE_BIGINTEGER.equalsIgnoreCase(validateType)) {
                    BigInteger.valueOf(Long.parseLong(input));

                } else if (APPLICATION_VALUE_TYPE_BOOLEAN.equalsIgnoreCase(validateType)) {
                    return "true".equalsIgnoreCase(input) || "false".equalsIgnoreCase(input);
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public static boolean validatePosition(String input, String pattern, int length) {
        return (input.indexOf(pattern, length) == length);
    }

    public static boolean verifyDateFormat(String input) {

        if (input != null) {
            try {
                java.util.Date ret = sdf.parse(input.trim());
                if (sdf.format(ret).equals(input.trim())) {
                    return true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
