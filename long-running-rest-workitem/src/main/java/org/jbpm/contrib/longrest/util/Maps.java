package org.jbpm.contrib.longrest.util;

import java.util.Map;

public class Maps {

    public static Map<String, Object> getStringObjectMap(Map<String, Object> map, String key) {
        return (Map<String, Object>) map.get(key);
    }

}
