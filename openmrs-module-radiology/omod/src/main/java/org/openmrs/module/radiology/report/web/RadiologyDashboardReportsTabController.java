/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.report.web;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.context.Context;
import org.openmrs.module.radiology.report.RadiologyReportStatus;
import org.openmrs.module.radiology.util.LogUtils;
import org.openmrs.module.radiology.web.RadiologyWebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for the dashboard tab containing {@code RadiologyReports}.
 */
@Controller
@RequestMapping(RadiologyDashboardReportsTabController.RADIOLOGY_REPORTS_TAB_REQUEST_MAPPING)
public class RadiologyDashboardReportsTabController {
    
    
    private static final Logger log = LoggerFactory.getLogger(RadiologyDashboardReportsTabController.class);
    
    public static final String RADIOLOGY_REPORTS_TAB_REQUEST_MAPPING = "/module/radiology/radiologyDashboardReportsTab.htm";
    
    static final String RADIOLOGY_REPORTS_TAB_VIEW = "/module/radiology/radiologyDashboardReportsTab";
    
    /**
     * Handles get requests for radiology reports tab page.
     * 
     * @return model and view of the radiology reports tab page
     * @should return model and view of the radiology reports tab page and set tab session attribute to radiology reports tab page
     */
    @RequestMapping(method = RequestMethod.GET)
    protected ModelAndView getRadiologyReportsTab(HttpServletRequest request) {
        
        final ModelAndView modelAndView = new ModelAndView(RADIOLOGY_REPORTS_TAB_VIEW);
        request.getSession()
                .setAttribute(RadiologyWebConstants.RADIOLOGY_DASHBOARD_TAB_SESSION_ATTRIBUTE,
                    RADIOLOGY_REPORTS_TAB_REQUEST_MAPPING);
        
        String patientId = request.getParameter("patientId");
        Map<String, String> logData = new LinkedHashMap<>();
        logData.put("user_uuid", Context.getAuthenticatedUser() != null ? Context.getAuthenticatedUser()
                .getUuid() : "unknown");
        logData.put("patient_id", patientId != null ? patientId : "unknown");
        logData.put("view", "reports");
        log.info(LogUtils.formatAsJson("dashboard_view", logData));
        
        return modelAndView;
    }
    
    /**
     * Returns entries for report status select element to filter radiology reports.
     * 
     * @return a map containing all report status values and an entry to select all report statuses
     * @should return a map containing all report status values and an entry to select all report statuses
     */
    @ModelAttribute("reportStatuses")
    protected Map<String, String> getReportStatusList() {
        
        final Map<String, String> reportStatuses = new HashMap<String, String>();
        reportStatuses.put("", "selectStatus");
        
        for (final RadiologyReportStatus status : RadiologyReportStatus.values()) {
            reportStatuses.put(status.name(), status.name());
        }
        
        return reportStatuses;
    }
    
}
