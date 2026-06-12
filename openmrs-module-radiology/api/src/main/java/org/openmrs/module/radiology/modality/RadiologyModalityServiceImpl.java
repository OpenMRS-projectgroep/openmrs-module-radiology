/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.modality;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.radiology.util.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
class RadiologyModalityServiceImpl extends BaseOpenmrsService implements RadiologyModalityService {
    
    
    private static final Logger log = LoggerFactory.getLogger(RadiologyModalityServiceImpl.class);
    
    private RadiologyModalityDAO radiologyModalityDAO;
    
    public void setRadiologyModalityDAO(RadiologyModalityDAO radiologyModalityDAO) {
        this.radiologyModalityDAO = radiologyModalityDAO;
    }
    
    /**
     * @see RadiologyModalityService#saveRadiologyModality(RadiologyModality)
     */
    @Override
    @Transactional
    public synchronized RadiologyModality saveRadiologyModality(RadiologyModality radiologyModality) {
        
        if (radiologyModality == null) {
            throw new IllegalArgumentException("radiologyModality cannot be null");
        }
        RadiologyModality result = radiologyModalityDAO.saveRadiologyModality(radiologyModality);
        
        Map<String, String> logData = new LinkedHashMap<>();
        logData.put("user_uuid", Context.getAuthenticatedUser() != null ? Context.getAuthenticatedUser()
                .getUuid() : "unknown");
        logData.put("modality_uuid", result.getUuid());
        log.info(LogUtils.formatAsJson("modality_saved", logData));
        
        return result;
    }
    
    /**
     * @see RadiologyModalityService#retireRadiologyModality(RadiologyModality, String)
     */
    @Override
    @Transactional
    public synchronized RadiologyModality retireRadiologyModality(RadiologyModality radiologyModality, String reason) {
        
        if (radiologyModality == null) {
            throw new IllegalArgumentException("radiologyModality cannot be null");
        }
        if (StringUtils.isBlank(reason)) {
            throw new IllegalArgumentException(Context.getMessageSourceService()
                    .getMessage("general.voidReason.empty"));
        }
        RadiologyModality result = radiologyModalityDAO.saveRadiologyModality(radiologyModality);
        
        Map<String, String> logData = new LinkedHashMap<>();
        logData.put("user_uuid", Context.getAuthenticatedUser() != null ? Context.getAuthenticatedUser()
                .getUuid() : "unknown");
        logData.put("modality_uuid", result.getUuid());
        logData.put("reason", reason);
        log.info(LogUtils.formatAsJson("modality_retired", logData));
        
        return result;
    }
    
    /**
     * @see RadiologyModalityService#getRadiologyModality(Integer)
     */
    @Override
    public RadiologyModality getRadiologyModality(Integer id) {
        
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        return radiologyModalityDAO.getRadiologyModality(id);
    }
    
    /**
     * @see RadiologyModalityService#getRadiologyModalityByUuid(String)
     */
    @Override
    public RadiologyModality getRadiologyModalityByUuid(String uuid) {
        
        if (uuid == null) {
            throw new IllegalArgumentException("uuid cannot be null");
        }
        return radiologyModalityDAO.getRadiologyModalityByUuid(uuid);
    }
    
    /**
     * @see RadiologyModalityService#getRadiologyModalities(boolean)
     */
    @Override
    public List<RadiologyModality> getRadiologyModalities(boolean includeRetired) {
        
        return radiologyModalityDAO.getRadiologyModalities(includeRetired);
    }
}
