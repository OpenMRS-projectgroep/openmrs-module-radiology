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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Tests {@link LogUtils}.
 */
public class LogUtilsTest {
    
    
    @Test
    public void formatAsJson_shouldIncludeEventAndDataFields() {
        
        Map<String, String> data = new LinkedHashMap<>();
        data.put("user_uuid", "abc-123");
        data.put("modality_uuid", "def-456");
        
        String json = LogUtils.formatAsJson("modality_saved", data);
        
        assertTrue(json.contains("\"event\":\"modality_saved\""));
        assertTrue(json.contains("\"user_uuid\":\"abc-123\""));
        assertTrue(json.contains("\"modality_uuid\":\"def-456\""));
    }
    
    @Test
    public void formatAsJson_shouldOmitDataSectionWhenMapIsEmpty() {
        
        String json = LogUtils.formatAsJson("invalid_dicom_uid_attempt", Collections.emptyMap());
        
        assertTrue(json.contains("\"event\":\"invalid_dicom_uid_attempt\""));
        assertFalse(json.contains("user_uuid"));
    }
}
