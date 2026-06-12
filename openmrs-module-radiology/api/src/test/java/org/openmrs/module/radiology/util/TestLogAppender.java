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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * A reusable Log4j 2 appender that captures log events during unit tests.
 * <p>
 * OpenMRS 2.x routes SLF4J through Log4j 2; this appender attaches to the active
 * {@link LoggerContext} so events emitted from Spring-managed services are captured.
 * <p>
 * Usage:
 * <pre>
 *   TestLogAppender appender = new TestLogAppender();
 *   appender.attach(MyClass.class);
 *   // ... execute code that logs ...
 *   List&lt;LogEvent&gt; events = appender.getEvents();
 *   appender.detach();
 * </pre>
 */
public class TestLogAppender {
    
    
    private final List<LogEvent> events = new ArrayList<>();
    
    private final String appenderName = "TestLogAppender-" + UUID.randomUUID();
    
    private Appender appender;
    
    private LoggerConfig loggerConfig;
    
    private Level originalLevel;
    
    private String attachedLoggerName;
    
    private boolean createdDedicatedLogger;
    
    /**
     * Attach this appender to the Log4j 2 logger for the given class.
     * Sets the logger level to ALL so that all log events (including INFO)
     * are captured, regardless of the test environment configuration.
     *
     * @param clazz the class whose logger should be intercepted
     */
    public void attach(Class<?> clazz) {
        attachedLoggerName = clazz.getName();
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = context.getConfiguration();
        
        appender =
                new AbstractAppender(appenderName, null, PatternLayout.createDefaultLayout(), false, Property.EMPTY_ARRAY) {
                    
                    
                    @Override
                    public void append(LogEvent event) {
                        if (attachedLoggerName != null && attachedLoggerName.equals(event.getLoggerName())) {
                            events.add(event.toImmutable());
                        }
                    }
                };
        appender.start();
        configuration.addAppender(appender);
        
        loggerConfig = configuration.getLoggerConfig(attachedLoggerName);
        originalLevel = loggerConfig.getLevel();
        createdDedicatedLogger = !loggerConfig.getName()
                .equals(attachedLoggerName);
        if (createdDedicatedLogger) {
            loggerConfig = new LoggerConfig(attachedLoggerName, Level.ALL, false);
            configuration.addLogger(attachedLoggerName, loggerConfig);
        } else {
            loggerConfig.setLevel(Level.ALL);
        }
        loggerConfig.addAppender(appender, Level.ALL, null);
        context.updateLoggers();
    }
    
    /**
     * Detach this appender from the logger it was attached to, restore the original level,
     * and clear captured events.
     */
    public void detach() {
        if (appender == null) {
            return;
        }
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = context.getConfiguration();
        if (loggerConfig != null) {
            loggerConfig.removeAppender(appender.getName());
            if (createdDedicatedLogger) {
                configuration.removeLogger(attachedLoggerName);
            } else {
                loggerConfig.setLevel(originalLevel);
            }
        }
        appender.stop();
        context.updateLoggers();
        appender = null;
        loggerConfig = null;
        attachedLoggerName = null;
        createdDedicatedLogger = false;
        events.clear();
    }
    
    /**
     * Returns the list of all {@link LogEvent}s captured since the last {@link #attach(Class)}
     * or {@link #detach()} call.
     *
     * @return captured events
     */
    public List<LogEvent> getEvents() {
        return events;
    }
    
    /**
     * Checks whether any captured log message contains the given substring.
     *
     * @param substring the text to look for
     * @return true if at least one captured event's message contains {@code substring}
     */
    public boolean hasMessageContaining(String substring) {
        return events.stream()
                .map(e -> e.getMessage()
                        .getFormattedMessage())
                .anyMatch(msg -> msg.contains(substring));
    }
    
    /**
     * Checks whether any captured event has the given Log4j 2 level.
     *
     * @param level the expected level
     * @return true if at least one captured event matches the level
     */
    public boolean hasEventAtLevel(Level level) {
        return events.stream()
                .anyMatch(e -> e.getLevel()
                        .equals(level));
    }
}
