package org.jbpm.contrib.restservice.util;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Json {
    public static Object escape(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof String) {
            return StringEscapeUtils.escapeJson((String) o);
        } else if (o instanceof Map) {
            Map<?, ?> m = (Map)o;
            Map result = new HashMap();
            for (Map.Entry e : m.entrySet()) {
                result.put(escape(e.getKey()), escape(e.getValue()));
            }
            return result;
        } else if (o instanceof List) {
            return ((List<?>) o).stream()
                    .map(e -> escape(e))
                    .collect(Collectors.toList());
        } else if (o instanceof Set) {
            return ((Set<?>) o).stream()
                    .map(e -> escape(e))
                    .collect(Collectors.toSet());
        } else {
            return o;
        }
    }

    public static Object unescape(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof String) {
            return StringEscapeUtils.unescapeJson((String) o);
        } else if (o instanceof Map) {
            Map<?, ?> m = (Map)o;
            Map result = new HashMap();
            for (Map.Entry e : m.entrySet()) {
                result.put(unescape(e.getKey()), unescape(e.getValue()));
            }
            return result;
        } else if (o instanceof List) {
            return ((List<?>) o).stream()
                    .map(e -> unescape(e))
                    .collect(Collectors.toList());
        } else if (o instanceof Set) {
            return ((Set<?>) o).stream()
                    .map(e -> unescape(e))
                    .collect(Collectors.toSet());
        } else {
            return o;
        }
    }
}
