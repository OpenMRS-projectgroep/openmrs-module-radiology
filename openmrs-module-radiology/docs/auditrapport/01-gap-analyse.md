# NEN-7510 Gap-analyse: OpenMRS Radiology Module

Dit document beschrijft de gap-analyse van de **OpenMRS Radiology Module** ten opzichte van de **NEN-7510:2024-2** norm. Hierin evalueren we in hoeverre de module voldoet aan de gestelde controls en leveren we bewijslast uit de code.

---

## Overzicht van Controls

| Control ID | NEN-7510:2024 Control | Status | Samenvatting Bewijslast |
| :--- | :--- | :---: | :--- |
| **A.8.3** | **Toegangsbeveiliging** | **Aanwezig** | Privileges gedefinieerd via Liquibase, afgedwongen met `@Authorized` AOP-annotaties op de service-laag en `<openmrs:hasPrivilege>` tags in JSP-bestanden. |
| **A.8.5** | **Authenticatie** | **Gedeeltelijk** | De module delegeert alle gebruikersauthenticatie en sessiebeheer aan OpenMRS Core. Echter is er een kritiek beveiligingslek ontdekt met hardcoded PACS-beheerdersgegevens in de code (`RadiologyActivator.java`). |
| **A.8.15** | **Logging** | **Gedeeltelijk** | OpenMRS database-auditing (wie, wat, wanneer) is volledig aanwezig voor alle entiteiten via Hibernate. Echter ontbreekt applicatie-logging voor klinische events en lekt de enige actieve logmethode gevoelige patiëntgegevens (PII) in platte tekst. |

---

## Control A.8.3: Toegangsbeveiliging (Access Control)

### 1. Status: **Aanwezig**

### 2. Beschrijving van Implementatie
De toegangsbeveiliging van de Radiology-module is gebaseerd op het Role-Based Access Control (RBAC) model van de OpenMRS Core. Dit model bestaat uit drie niveaus:
1. **Gebruikers (Users)**: Worden gekoppeld aan rollen.
2. **Rollen (Roles)**: Groeperen specifieke privileges (bijv. Refererend arts, Radioloog, Planner).
3. **Privileges (Rechten)**: De meest fijnmazige rechten die bepalen of een gebruiker een specifieke actie (lezen, schrijven, bewerken, verwijderen) mag uitvoeren.

De module dwingt deze toegangsbeveiliging af op zowel de **Service-laag (Java API)** als op de **User Interface-laag (JSP-pagina's)**.

---

### 3. Bewijslast (Evidence)

#### A. Databaseinrichting (Rollen en Privileges)
De benodigde rollen en privileges voor de radiology-module worden tijdens de installatie automatisch in de OpenMRS-database aangemaakt via Liquibase-migraties. 
* **Bestand:** [liquibase.xml](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/resources/liquibase.xml)
  * **Rollen (Regels 137–157):** Hier worden vier specifieke rollen gedefinieerd:
    * `Radiology: Referring physician`
    * `Radiology: Reading physician`
    * `Radiology: Performing physician`
    * `Radiology: Scheduler`
  * **Privileges (Regels 300-346, 369-375, 411-432, 439-455, 558-570):** Hier worden fijnmazige privileges toegevoegd, zoals:
    * `Add Radiology Orders` (Regel 334)
    * `Get Radiology Orders` (Regel 342)
    * `Add Radiology Reports` (Regel 303)
    * `Get Radiology Reports` (Regel 318)
    * `Add Radiology Studies` (Regel 441)
    * `Manage Radiology Modalities` (Regel 566)

#### B. API & Service-laag (Java `@Authorized` Annotatie)
De service-laag van de API gebruikt Spring AOP met de `@Authorized` annotatie van OpenMRS. Als een methode wordt aangeroepen, valideert OpenMRS of de huidige ingelogde gebruiker het vereiste privilege bezit. Zo niet, dan wordt een `APIAuthenticationException` opgeworpen.
* **Privilege Definities:** [RadiologyPrivileges.java](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/java/org/openmrs/module/radiology/RadiologyPrivileges.java) bevat alle privilege-constanten.
* **Voorbeelden in de code:**
  * In [RadiologyStudyService.java](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/java/org/openmrs/module/radiology/study/RadiologyStudyService.java#L43):
    ```java
    @Authorized(RadiologyPrivileges.ADD_RADIOLOGY_STUDIES)
    RadiologyStudy saveRadiologyStudy(RadiologyStudy study);
    ```
  * In [RadiologyOrderService.java](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/java/org/openmrs/module/radiology/order/RadiologyOrderService.java#L54):
    ```java
    @Authorized(RadiologyPrivileges.ADD_RADIOLOGY_ORDERS)
    RadiologyOrder saveRadiologyOrder(RadiologyOrder radiologyOrder);
    ```
  * In [RadiologyReportService.java](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/java/org/openmrs/module/radiology/report/RadiologyReportService.java#L56):
    ```java
    @Authorized(RadiologyPrivileges.ADD_RADIOLOGY_REPORTS)
    RadiologyReport saveRadiologyReport(RadiologyReport radiologyReport);
    ```

#### C. Presentatie-laag (JSP-bestanden)
De UI-laag maakt gebruik van de OpenMRS JSP tag library om knoppen, formulieren en links af te schermen voor gebruikers die de bijbehorende privileges niet bezitten.
* **Voorbeeld in JSP:** 
  * In [radiologyDashboardOrdersTab.jsp](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/omod/src/main/webapp/radiologyDashboardOrdersTab.jsp#L327):
    ```jsp
    <openmrs:hasPrivilege privilege="Add Radiology Orders">
        <%-- Knop of formulier om orders toe te voegen --%>
    </openmrs:hasPrivilege>
    ```

#### D. Koppeling in tests
Tijdens tests worden rollen en privileges expliciet gekoppeld om de werking te valideren.
* **Bestand:** [demo-data.sql](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/acceptanceTest/resources/demo-data.sql#L82)
  ```sql
  INSERT INTO `role_privilege` VALUES ('Radiology: Referring physician','Add Radiology Orders');
  ```

---

## Control A.8.5: Authenticatie (Authentication)

### 1. Status: **Gedeeltelijk** (Door delegatie naar OpenMRS Core, maar bevat een kritiek lek in de module-code)

### 2. Beschrijving van Implementatie
De Radiology-module implementeert zelf **geen** eigen login-schermen, wachtwoordbeleid of sessiebeheer. Alle authenticatie van eindgebruikers wordt volledig gedelegeerd aan de **OpenMRS Core**. 
* **Gebruikersauthenticatie:** Wanneer een gebruiker inlogt, valideert OpenMRS Core de inloggegevens (wachtwoorden worden standaard gehasht met salt met SHA-512 of Bcrypt in de `users`-tabel).
* **Sessiebeheer:** Sessies worden beheerd op applicatieserver-niveau (bijv. Tomcat) via een standaard Java `HttpSession`. De ingelogde gebruiker is via `Context.getAuthenticatedUser()` in de backend en `${authenticatedUser}` in JSP-bestanden beschikbaar.
* **REST API-authenticatie:** De module delegeert de REST-beveiliging aan de OpenMRS Web Services REST-module (`org.openmrs.module.webservices.rest`). Deze module ondersteunt sessie-gebaseerde authenticatie en HTTP Basic Authentication voor API-endpoints.

---

### 3. Geïdentificeerd Beveiligingsrisico (Gap)

Tijdens de gap-analyse is er een ernstig beveiligingslek ontdekt in de code van de Radiology-module. Er zijn hardcoded inloggegevens (credentials) opgenomen voor de admin-toegang tot de PACS/DICOM server in de activator class. Dit schendt het basisprincipe van NEN-7510 / ISO 27001 met betrekking tot het veilig opslaan en beheren van authenticatiegegevens.

* **Locatie in de code:** [RadiologyActivator.java](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/java/org/openmrs/module/radiology/RadiologyActivator.java#L45-L50)
  ```java
  // PACS/DICOM server admin credentials — used for worklist push configuration
  private static final String PACS_HOST = "pacs.hospital.internal";
  private static final String PACS_ADMIN_USER = "radiology_admin";
  private static final String PACS_ADMIN_PASSWORD = "PACS@dm1n2021!";
  private static final String PACS_WADO_URL = "http://pacs.hospital.internal:8080/wado?requestType=WADO";
  ```

* **Risico:** Iedereen met toegang tot de broncode of de gecompileerde JAR heeft direct toegang tot de PACS-server met administrator-rechten.

* **Aanbevolen maatregel:** Verwijder deze hardcoded inloggegevens en vervang ze door OpenMRS Global Properties (instellingen die via de database worden beheerd en via de OpenMRS Admin UI kunnen worden geconfigureerd) of via omgevingsvariabelen (Environment Variables).

---

## Control A.8.15: Logging (Activity Logging)

### 1. Status: **Gedeeltelijk** (Database-auditing is aanwezig via OpenMRS Core, maar applicatie-logging lekt PII en mist structuur)

### 2. Beschrijving van Implementatie
De audit-logging binnen de Radiology-module is opgedeeld in twee categorieën: database-auditing en applicatielogs (bestanden).

#### A. Database-auditing (OpenMRS Core / Hibernate Interceptors)
OpenMRS dwingt op database-niveau een strikte audit-trail af voor alle klinische entiteiten. Alle entiteiten in deze module (zoals `RadiologyStudy`, `RadiologyReport`, `RadiologyModality` en `MrrtReportTemplate`) erven van `BaseOpenmrsData` of `BaseOpenmrsObject`.
* **Automatische metadata-registratie:** Bij elke creatie, wijziging of verwijdering (voiding/retiring) legt OpenMRS Core via Hibernate Interceptors automatisch de volgende gegevens vast in de database:
  * `creator` (de gebruiker die het record heeft aangemaakt)
  * `date_created` (tijdstip van aanmaak)
  * `changed_by` & `date_changed` (tijdstip en auteur van de laatste wijziging)
  * `voided`/`retired` (of het record logisch is verwijderd)
  * `voided_by`/`retired_by` & `date_voided`/`date_retired` (wie het record heeft verwijderd en wanneer)
  * `void_reason`/`retire_reason` (verplichte reden van verwijdering)
* **Bewijslast in de code:** In de database-definities ([liquibase.xml](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/resources/liquibase.xml)) zijn deze audit-kolommen voor alle radiology-tabellen aanwezig. Zie bijvoorbeeld de definitie van `radiology_report`:
  * **Aanmaak- en wijzigings-metadata (regels 256–261):**
    ```xml
    <column name="creator" type="int" defaultValueNumeric="0">
        <constraints nullable="false" />
    </column>
    <column name="date_created" type="DATETIME">
        <constraints nullable="false" />
    </column>
    ```
  * **Verwijderings-metadata (regels 463–471 voor voiding):**
    ```xml
    <addColumn tableName="radiology_report">
        <column name="voided" type="BOOLEAN" defaultValueBoolean="false" >
            <constraints nullable="false" />
        </column>
        <column name="date_voided" type="DATETIME"/>
        <column name="voided_by" type="int"/>
        <column name="void_reason" type="varchar(255)"/>
    </addColumn>
    ```


#### B. Applicatie-logging (Logbestanden)
De module maakt gebruik van de SLF4J-logging-faciliteit. In de service-implementaties (`*ServiceImpl.java`) wordt echter vrijwel **geen** gebruik gemaakt van applicatie-logging om klinische handelingen (zoals het voltooien van een rapport of het aanmaken van een studie) naar de server-logbestanden te schrijven.

#### C. Formele NEN-7510 Logging Matrix
De onderstaande matrix toetst de belangrijkste logging-events binnen de Radiology-module kritisch aan de eisen van **NEN-7510-2:2024 control A.8.15 / A.12.4**. Hierbij is specifiek getoetst of de noodzakelijke metadata (**wie** heeft de actie uitgevoerd, **wat** is er gebeurd, **wanneer** vond het plaats en indien van toepassing **waarom**) aanwezig is in de logs.

| Event | Gelogd? | Gevoelige data | Compliant met NEN-7510 8.15? |
| :--- | :--- | :---: | :--- |
| **Gebruiker authenticatie (Inloggen / Uitloggen)** | **Ja** (via OpenMRS Core database/applicatielogs). | Nee | **Ja**. OpenMRS Core registreert inlogpogingen en mislukte logins met volledige metadata: *wie* (gebruikersnaam/IP), *wat* (succes/mislukt), *wanneer* (timestamp) en *waarom* (foutreden bij mislukking). |
| **Raadplegen patiëntendossier / inzien radiology dashboard** | **Nee**. Noch in database-audit, noch in applicatielogs. | Ja | **Nee**. NEN-7510 vereist dat inzage in patiëntgegevens herleidbaar is (read auditing). Het ontbreken van logging voor dit event betekent dat er geen metadata (*wie*, *wat*, *wanneer*) over dossierinzage beschikbaar is. |
| **Radiology Order aanmaken (placing order)** | **Gedeeltelijk**. Alleen via database-auditing. Applicatielogs loggen dit niet omdat `logRadiologyOrderSubmission` inactief is. | Nee | **Nee**. Database-auditing legt wel *wie* (`creator`), *wat* (order) en *wanneer* (`date_created`) vast. Echter is applicatielogging voor realtime SIEM-monitoring inactief en ontbreekt de reden (*waarom*) voor creatie in de audit-trail. Activering van de huidige applicatielogmethode zou bovendien tot een PII-lek leiden. |
| **Radiology Order inzien** | **Nee**. Noch in database-audit, noch in applicatielogs. | Ja | **Nee**. Geen logging op lees-events van individuele orders. *Wie* de order heeft ingezien en *wanneer* is niet herleidbaar. |
| **Radiology Order stopzetten / wijzigen (discontinue)** | **Gedeeltelijk**. Database legt mutatie en reden vast via OpenMRS Core. Geen applicatielogging. | Nee | **Gedeeltelijk**. De database bevat volledige metadata: *wie* (`changed_by`), *wat* (stopzetting), *wanneer* (`date_changed`) en *waarom* (`nonCodedDiscontinueReason`). Het ontbreken van realtime applicatielogging voor externe security monitoring (SIEM) vormt echter een gap. |
| **Radiology Rapport aanmaken / opslaan (concept/draft)** | **Gedeeltelijk**. Alleen database-auditing. Geen applicatielogging. | Nee | **Gedeeltelijk**. Database-auditing legt *wie* (`creator`), *wat* (rapport) en *wanneer* (`date_created`) vast. De reden (*waarom*) is bij normale invoer van concepten niet van toepassing. Echter is er geen realtime applicatie-audit trail. |
| **Radiology Rapport inzien** | **Nee**. Noch in database-audit, noch in applicatielogs. | Ja | **Nee**. Geen read-auditing op rapporten. *Wie* het rapport heeft ingezien en *wanneer* is niet herleidbaar. |
| **Radiology Rapport definitief voltooien** | **Gedeeltelijk**. Alleen database-auditing. Geen applicatielogging. | Nee | **Gedeeltelijk**. Database-auditing legt *wie* (`changed_by`), *wat* (statuswijziging naar `COMPLETED`) en *wanneer* (`date_changed`) vast. Geen applicatielogging. |
| **Radiology Rapport intrekken / verwijderen (voiden)** | **Ja** (via database). Database legt alle metadata vast. Geen applicatielogging. | Nee | **Ja** (voor database-auditing). Metadata is volledig aanwezig: *wie* (`voided_by`), *wat* (voiding van rapport), *wanneer* (`date_voided`) en de verplichte reden *waarom* (`void_reason`). Alleen het gebrek aan een realtime melding in de applicatielogs is een aandachtspunt. |
| **Radiology Studie opslaan / aanmaken** | **Gedeeltelijk**. Alleen database-auditing. Geen applicatielogging. | Nee | **Gedeeltelijk**. Database-auditing legt *wie* (`creator`), *wat* (studie) en *wanneer* (`date_created`) vast. Geen applicatielogging. |
| **Beheer van modaliteiten (CRUD)** | **Gedeeltelijk**. Alleen database-auditing. Geen applicatielogging. | Nee | **Gedeeltelijk**. Database-auditing legt *wie* (`creator` / `changed_by`), *wat* (modality) en *wanneer* (`date_created` / `date_changed`) vast. Geen administratieve applicatielogging. |
| **Beheer van templates (CRUD)** | **Gedeeltelijk**. Database-auditing legt mutaties vast. Applicatielogs registreren alleen fouten. | Nee | **Gedeeltelijk**. Mutaties worden in de database geaudit met *wie*, *wat*, *wanneer*. Applicatielogs bevatten alleen debug/error details en geen functionele audit-logs van succesvolle acties. |
| **Systeemopstart en netwerk/PACS configuratie** | **Ja** (via applicatielogs). | Nee | **Ja**. Systeem logt opstartfase en geladen PACS hosts. Metadata: *wat* (opstartfase/configuratie), *wanneer* (log timestamp). *Wie* is hier de applicatieserver zelf. |
| **Systeemfouten en data-binding exceptions** | **Ja** (via applicatielogs). | Nee | **Ja**. Technische bindings- en XML-validatiefouten worden gelogd onder `ERROR`-niveau. Metadata: *wat* (foutmelding/stacktrace) en *wanneer* (log timestamp) zijn aanwezig. |

---

### 3. Geïdentificeerd Beveiligingsrisico (Gap)

Er is een ernstig privacy- en compliance-risico (lekken van PII) geïdentificeerd in de enige methode die klinische acties naar de logbestanden schrijft:

* **Locatie in de code:** [RadiologyOrderServiceImpl.java](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/java/org/openmrs/module/radiology/order/RadiologyOrderServiceImpl.java#L212-L220)
  ```java
  public void logRadiologyOrderSubmission(RadiologyOrder order) {
      org.openmrs.Patient patient = order.getPatient();
      log.info("[RADIOLOGY] Order submitted:" + " patient=" + patient.getPersonName() + " dob=" + patient.getBirthdate()
              + " identifier=" + (patient.getPatientIdentifier() != null ? patient.getPatientIdentifier()
                      .getIdentifier() : "none")
              + " modality=" + (order.getStudy() != null ? order.getStudy()
                      .getModality() : "unknown")
              + " scheduledDate=" + order.getScheduledDate() + " accessionNumber=" + order.getAccessionNumber());
  }
  ```

* **Risico (GDPR / NEN-7510):** Deze methode logt Personally Identifiable Information (PII) van de patiënt, zoals de volledige naam (`patient.getPersonName()`) en de geboortedatum (`patient.getBirthdate()`), in platte tekst naar de applicatielogbestanden (bijv. `catalina.out` van Tomcat). Applicatielogs worden vaak niet op hetzelfde beveiligingsniveau opgeslagen als de database en zijn toegankelijk voor systeembeheerders en ontwikkelaars die deze patiëntgegevens niet mogen inzien.
* **Bijkomend probleem:** Deze methode gebruikt de ongedefinieerde variabele `log` en de vervallen methode `.getModality()`, wat op dit moment zorgt voor de compileerfouten van de gehele module.

* **Aanbevolen maatregel:**
  1. Pas de logregel aan om **geen PII** meer te loggen. Log in plaats daarvan alleen technische identifiers (zoals de `patient.getUuid()` of `order.getUuid()`).
  2. Implementeer gestructureerde applicatielogging voor overige kritieke acties (zoals het bekijken of wijzigen van rapporten) zonder gevoelige gegevens te loggen.
  3. Los de compileerfouten op door de SLF4J logger correct te declareren en de niet-bestaande aanroep naar `.getModality()` te verwijderen.

---

### 4. Huidige versus Gewenste Situatie (Gap Analyse)

Om volledig te voldoen aan de NEN-7510 8.15 normering (en de AVG/GDPR), moet het gat tussen de huidige status van de module en de gewenste situatie overbrugd worden. Onderstaande tabel detailleert dit verschil en beschrijft de benodigde acties.

| Beveiligingsaspect | Huidige Situatie (As-Is) | Gewenste Situatie (To-Be) | Benodigde Actie(s) / Remediatie |
| :--- | :--- | :--- | :--- |
| **Inzage-auditing (Read Auditing)** | Er wordt **niets** gelogd wanneer een gebruiker patiëntgegevens inziet (zoals het openen van de radiology dashboard-tab, het raadplegen van orders of het inzien van rapporten). | Elke handmatige raadpleging of export van patiëntgegevens genereert een logregel op applicativeniveau (SIEM) met de metadata: *wie*, *wat* en *wanneer*. | Voeg log-statements toe aan controllers (`RadiologyReportFormController`, `RadiologyOrderDetailsPortletController`) en REST resources (`RadiologyReportResource`) bij ophaal-acties (GET). |
| **Mutatie-auditing in applicatielogs** | Mutaties (creëren, wijzigen, verwijderen) worden alleen in de database bijgehouden. Applicatielogging voor orders (`logRadiologyOrderSubmission`) is inactief (nooit aangeroepen). | Alle mutaties op orders en rapporten worden realtime weggeschreven naar de applicatielogs ten behoeve van SIEM-monitoring. | 1. Activeer en roep `logRadiologyOrderSubmission` aan in `placeRadiologyOrder`. <br>2. Voeg vergelijkbare logs toe bij `discontinueRadiologyOrder`, `saveRadiologyReport` en `voidRadiologyReport`. |
| **Bescherming van patiëntprivacy (PII)** | De inactieve methode `logRadiologyOrderSubmission` logt direct herleidbare patiëntgegevens (volledige naam en geboortedatum) in platte tekst. | Applicatielogs bevatten **geen direct herleidbare PII** (conform AVG). Koppeling vindt uitsluitend plaats via UUID's en accession numbers. | Vervang de parameters `patient.getPersonName()` en `patient.getBirthdate()` in de logmethode door `patient.getUuid()`. |
| **Logging van administratieve CRUD-acties** | Wijzigingen aan modaliteiten (bijv. MRI, CT) en rapporttemplates (MRRT) worden niet weggeschreven naar de applicatielogs. | Wijzigingen in de systeemconfiguratie of metadata-tabellen genereren een duidelijke `INFO` logregel in de applicatielogs met metadata. | Voeg `log.info` toe aan de bewerkings- en verwijderingsmethoden in de service-laag van modaliteiten en templates. |
| **Gestructureerd Logformaat** | Bestaande logregels zijn ongestructureerde tekstregels, wat automatische verwerking en monitoring bemoeilijkt. | Alle audit-events in de applicatielogs gebruiken een gestructureerd formaat (zoals JSON of key-value pairs) voor eenvoudige SIEM-parsing. | Implementeer een gestructureerde log-helper of format-sjabloon in de module (bijv. `event=... user=... target=...`). |
