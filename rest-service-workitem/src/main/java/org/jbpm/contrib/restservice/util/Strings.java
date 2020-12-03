package org.jbpm.contrib.restservice.util;

import java.util.*;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Strings {

    /**
     * Parse string (key1=value1;key2=value2) to Map<Key,Value></>.
     */
    public static Map<String, String> toMap(String string) {
        if (string == null || string.equals("")) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<>();
        try {
            String[] pairs = string.split(";");

            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                map.put(keyValue[0], keyValue[1]);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Invalid key=value string: [" + string + "]", e);
        }
        return map;
    }

    /**
     * Adds ending slash if it is not present.
     *
     * @param string
     * @return
     */
    public static String addEndingSlash(String string) {
        if (string == null) {
            return null;
        }
        if (!string.endsWith("/")) {
            string += "/";
        }
        return string;
    }

    public static boolean isEmpty(String string) {
        if (string == null) {
            return true;
        } else {
            return "".equals(string);
        }
    }

    public static Object quoteString(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            return "\"" + o + "\"";
        } else {
            return o;
        }
    }
}
