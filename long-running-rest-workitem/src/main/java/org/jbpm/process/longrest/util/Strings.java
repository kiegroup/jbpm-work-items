/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.process.longrest.util;

import java.util.*;

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
