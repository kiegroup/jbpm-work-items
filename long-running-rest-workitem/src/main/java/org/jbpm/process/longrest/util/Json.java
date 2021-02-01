package org.jbpm.process.longrest.util;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Json {
    public static <T> T escape(T o) {
        if (o == null) {
            return null;
        } else if (o instanceof String) {
            return (T) StringEscapeUtils.escapeJson((String) o);
        } else if (o instanceof Map) {
            Map<?, ?> m = (Map)o;
            Map result = new HashMap();
            for (Map.Entry e : m.entrySet()) {
                result.put(escape(e.getKey()), escape(e.getValue()));
            }
            return (T) result;
        } else if (o instanceof List) {
            return (T) ((List<?>) o).stream()
                    .map(e -> escape(e))
                    .collect(Collectors.toList());
        } else if (o instanceof Set) {
            return (T) ((Set<?>) o).stream()
                    .map(e -> escape(e))
                    .collect(Collectors.toSet());
        } else {
            return o;
        }
    }

    public static <T> T unescape(T o) {
        if (o == null) {
            return null;
        } else if (o instanceof String) {
            return (T) StringEscapeUtils.unescapeJson((String) o);
        } else if (o instanceof Map) {
            Map<?, ?> m = (Map)o;
            Map result = new HashMap();
            for (Map.Entry e : m.entrySet()) {
                result.put(unescape(e.getKey()), unescape(e.getValue()));
            }
            return (T) result;
        } else if (o instanceof List) {
            return (T) ((List<?>) o).stream()
                    .map(e -> unescape(e))
                    .collect(Collectors.toList());
        } else if (o instanceof Set) {
            return (T) ((Set<?>) o).stream()
                    .map(e -> unescape(e))
                    .collect(Collectors.toSet());
        } else {
            return o;
        }
    }
}
