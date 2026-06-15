/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.order;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail; // Toegevoegd voor de gevoelige data check
import static org.openmrs.module.radiology.test.ValidatorAssertions.assertSingleErrorInField;
import static org.openmrs.module.radiology.test.ValidatorAssertions.assertSingleGeneralError;
import static org.openmrs.module.radiology.test.ValidatorAssertions.assertSingleNullErrorInField;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

// Log4j imports om logs in-memory op te vangen
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import java.io.StringWriter;

import org.junit.After; // Toegevoegd voor cleanup
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PersonName; // Toegevoegd voor het mocken van persoonsgegevens
import org.openmrs.Provider;
import org.openmrs.order.OrderUtilTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests {@link RadiologyOrderValidator}.
 */
public class RadiologyOrderValidatorTest {

    private RadiologyOrderValidator radiologyOrderValidator;
    private RadiologyOrder radiologyOrder;
    private Errors errors;

    // Variabelen voor het onderscheppen van logs
    private StringWriter logOutput;
    private WriterAppender appender;
    private Logger rootLogger;

    @Before
    public void setUp() {
        radiologyOrderValidator = new RadiologyOrderValidator();
        radiologyOrder = getValidRadiologyOrder();
        errors = new BindException(radiologyOrder, "radiologyOrder");

        // Richt de log-interceptor in vóór elke test
        logOutput = new StringWriter();
        appender = new WriterAppender(new org.apache.log4j.PatternLayout("%m%n"), logOutput);
        rootLogger = Logger.getRootLogger();
        rootLogger.addAppender(appender);
    }

    @After
    public void tearDown() {
        // Schoon de appender op om memory-leaks en vervuiling tussen tests te voorkomen
        if (rootLogger != null && appender != null) {
            rootLogger.removeAppender(appender);
        }
    }

    /**
     * Creates a valid RadiologyOrder.
     */
    private RadiologyOrder getValidRadiologyOrder() {
        RadiologyOrder radiologyOrder = new RadiologyOrder();
        radiologyOrder.setOrderer(new Provider());

        // Geef de patient een standaard, veilige dummy naam mee
        Patient patient = new Patient();
        PersonName dummyName = new PersonName("John", "", "Doe");
        patient.addName(dummyName);
        radiologyOrder.setPatient(patient);

        radiologyOrder.setConcept(new Concept(88));
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
        radiologyOrder.setDateActivated(cal.getTime());
        radiologyOrder.setAutoExpireDate(new Date());
        radiologyOrder.setUrgency(RadiologyOrder.Urgency.ROUTINE);
        radiologyOrder.setAction(RadiologyOrder.Action.NEW);
        return radiologyOrder;
    }

    /**
     * ACCEPTATIECRITERIUM TEST:
     * Controleert of er absoluut geen gevoelige gegevens in de logberichten
     * belanden
     * zodra er validatie-events of verwerkingen plaatsvinden.
     */
    @Test
    public void shouldNotLogSensitiveDataWhenValidationFails() throws Exception {

        // 1. Arrange: Breng het object in een staat met gevoelige (gesimuleerde
        // productie) data
        String gevoeligeNaam = "Abderabbouh Jansen";
        String medischeStatuscode = "Z94.0"; // Voorbeeld van een ICD-10 medische code (Niertransplantatiestatus)

        PersonName personName = new PersonName("Abderabbouh", "", "Jansen");
        radiologyOrder.getPatient().addName(personName);

        // Stel een concept in dat we als 'gevoelige medische code string' gaan
        // misbruiken in omschrijvingen
        Concept sensitiveConcept = new Concept(999);
        sensitiveConcept.setRetireReason(medischeStatuscode);
        radiologyOrder.setConcept(sensitiveConcept);

        // Maak het object ongeldig om logging/foutafhandeling te triggeren
        radiologyOrder.setAction(null);

        // 2. Act: Voer de validatie uit die mogelijk logs genereert
        radiologyOrderValidator.validate(radiologyOrder, errors);

        // Haal de opgevangen logs op
        String gegenereerdeLogs = logOutput.toString();

        // 3. Assert: Definieer patronen waar de logstring NOOIT aan mag voldoen
        Pattern naamPatroon = Pattern.compile(Pattern.quote(gevoeligeNaam), Pattern.CASE_INSENSITIVE);
        Pattern medischeCodePatroon = Pattern.compile("^[A-Z]\\d{2}(\\.\\d)?$"); // Matcht ICD-10 codes zoals Z94.0 of
                                                                                 // S42

        // Controleer op de plaintext naam
        if (naamPatroon.matcher(gegenereerdeLogs).find()) {
            fail("BEVEILIGINGSLEK: Plaintext patiëntnaam '" + gevoeligeNaam + "' aangetroffen in de logstream! Logs: "
                    + gegenereerdeLogs);
        }

        // Controleer op de specifieke medische statuscode
        if (gegenereerdeLogs.contains(medischeStatuscode)) {
            fail("BEVEILIGINGSLEK: Gevoelige medische statuscode '" + medischeStatuscode
                    + "' aangetroffen in de logstream! Logs: " + gegenereerdeLogs);
        }
    }

    // --- Hieronder staan je overige bestaande testen ongewijzigd ---

    @Test
    public void shouldReturnFalseForOtherObjectTypes() throws Exception {
        assertFalse(radiologyOrderValidator.supports(Object.class));
    }

    @Test
    public void shouldReturnTrueForRadiologyOrderObjects() throws Exception {
        assertTrue(radiologyOrderValidator.supports(RadiologyOrder.class));
    }

    @Test
    public void shouldFailValidationIfActionIsNull() throws Exception {
        radiologyOrder.setAction(null);
        radiologyOrderValidator.validate(radiologyOrder, errors);
        assertSingleNullErrorInField(errors, "action");
    }

    @Test
    public void shouldFailValidationIfConceptIsNull() throws Exception {
        radiologyOrder.setConcept(null);
        radiologyOrderValidator.validate(radiologyOrder, errors);
        assertSingleErrorInField(errors, "concept", "Concept.noConceptSelected");
    }

    @Test
    public void shouldFailValidationIfDateActivatedAfterAutoExpireDate() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
        radiologyOrder.setDateActivated(new Date());
        radiologyOrder.setAutoExpireDate(cal.getTime());
        radiologyOrderValidator.validate(radiologyOrder, errors);
        assertTrue(errors.hasFieldErrors("dateActivated"));
        assertTrue(errors.hasFieldErrors("autoExpireDate"));
    }

    @Test
    public void shouldFailValidationIfDateActivatedAfterDateStopped() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
        radiologyOrder.setDateActivated(new Date());
        OrderUtilTest.setDateStopped(radiologyOrder, cal.getTime());
        radiologyOrderValidator.validate(radiologyOrder, errors);
        assertTrue(errors.hasFieldErrors("dateActivated"));
        assertTrue(errors.hasFieldErrors("dateStopped"));
    }

    @Test
    public void shouldFailValidationIfOrdererIsNull() throws Exception {
        radiologyOrder.setOrderer(null);
        radiologyOrderValidator.validate(radiologyOrder, errors);
        assertSingleNullErrorInField(errors, "orderer");
    }

    @Test
    public void shouldFailValidationIfPatientIsNull() throws Exception {
        radiologyOrder.setPatient(null);
        radiologyOrderValidator.validate(radiologyOrder, errors);
        assertSingleNullErrorInField(errors, "patient");
    }

    @Test
    public void shouldFailValidationIfRadiologyOrderIsNull() throws Exception {
        radiologyOrderValidator.validate(null, errors);
        assertSingleGeneralError(errors);
    }

    @Test
    public void shouldFailValidationIfScheduledDateIsNullWhenUrgencyIsOnScheduledDate() throws Exception {
        radiologyOrder.setScheduledDate(null);
        radiologyOrder.setUrgency(RadiologyOrder.Urgency.ON_SCHEDULED_DATE);
        radiologyOrderValidator.validate(radiologyOrder, errors);
        assertSingleErrorInField(errors, "scheduledDate", "Order.error.scheduledDateNullForOnScheduledDateUrgency");
    }

    @Test
    public void shouldFailValidationIfScheduledDateIsSetAndUrgencyIsStat() throws Exception {
        radiologyOrder.setScheduledDate(new Date());
        radiologyOrder.setUrgency(RadiologyOrder.Urgency.STAT);
        radiologyOrderValidator.validate(radiologyOrder, errors);
        assertSingleErrorInField(errors, "urgency", "Order.error.urgencyNotOnScheduledDate");
    }

    @Test
    public void shouldFailValidationIfScheduledDateIsSetAndUrgencyIsRoutine() throws Exception {
        radiologyOrder.setScheduledDate(new Date());
        radiologyOrder.setUrgency(RadiologyOrder.Urgency.ROUTINE);
        radiologyOrderValidator.validate(radiologyOrder, errors);
        assertSingleErrorInField(errors, "urgency", "Order.error.urgencyNotOnScheduledDate");
    }

    @Test
    public void shouldNotFailValidationIfScheduledDateIsSetAndUrgencyIsOnScheduledDate() throws Exception {
        radiologyOrder.setScheduledDate(new Date());
        radiologyOrder.setUrgency(RadiologyOrder.Urgency.ON_SCHEDULED_DATE);
        radiologyOrderValidator.validate(radiologyOrder, errors);
        assertFalse(errors.hasErrors());
        assertFalse(errors.hasFieldErrors("urgency"));
    }

    @Test
    public void shouldFailValidationIfUrgencyIsNull() throws Exception {
        radiologyOrder.setUrgency(null);
        radiologyOrderValidator.validate(radiologyOrder, errors);
        assertSingleNullErrorInField(errors, "urgency");
    }

    @Test
    public void shouldFailValidationIfVoidedIsNull() throws Exception {
        radiologyOrder.setVoided(null);
        radiologyOrderValidator.validate(radiologyOrder, errors);
        assertSingleNullErrorInField(errors, "voided");
    }

    @Test
    public void shouldNotAllowAFutureDateActivated() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        radiologyOrder.setDateActivated(cal.getTime());
        radiologyOrderValidator.validate(radiologyOrder, errors);
        assertSingleErrorInField(errors, "dateActivated", "Order.error.dateActivatedInFuture");
    }

    @Test
    public void shouldPassValidationIfAllFieldsAreCorrect() throws Exception {
        radiologyOrderValidator.validate(radiologyOrder, errors);
        assertFalse(errors.hasErrors());
    }
}