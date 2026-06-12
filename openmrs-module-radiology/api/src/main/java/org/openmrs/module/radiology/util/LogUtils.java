/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.util;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for structured audit logging in the Radiology module.
 */
public class LogUtils {
    
    
    /**
     * Formats the given event and data map into a JSON-styled string.
     * 
     * @param event The event name (e.g., "order_submitted")
     * @param data  A map of key-value pairs representing the log metadata.
     * @return A JSON-formatted string.
     */
    public static String formatAsJson(String event, Map<String, String> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"event\":\"")
                .append(event)
                .append("\"");
        
        if (data != null && !data.isEmpty()) {
            sb.append(",");
            String dataJson = data.entrySet()
                    .stream()
                    .map(e -> "\"" + e.getKey() + "\":\"" + (e.getValue() != null ? e.getValue() : "null") + "\"")
                    .collect(Collectors.joining(","));
            sb.append(dataJson);
        }
        
        sb.append("}");
        return sb.toString();
    }
}
