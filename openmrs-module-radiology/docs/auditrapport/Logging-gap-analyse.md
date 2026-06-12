# Gap-analyse & Inventarisatie 'Logging' (NEN-7510)
## OpenMRS Radiology Module

Dit document bevat de gap-analyse van de **OpenMRS Radiology Module** op het gebied van logging en audit-trails, met een specifieke focus op de eisen uit de **NEN-7510-2:2024 (Control A.8.15 / A.12.4)** normering voor informatiebeveiliging in de zorg.

---

## 1. Doel & Scope
Het doel van deze analyse is het in kaart brengen van alle actieve logging-statements in de broncode van de Radiology-module en te evalueren of de klinische en administratieve acties (zoals inloggen, orders inzien, orders wijzigen en rapporten opstellen) daadwerkelijk een logregel genereren. 

---

## 2. Inventarisatie van Actieve Logging-statements
De gehele module (`api`, `omod` en `acceptanceTest`) is gescand op actieve logging-statements (`log.info`, `log.debug`, `log.error`, `log.warn`, `log.trace`). 

Er zijn in totaal **17 actieve logging-statements** geïdentificeerd in de broncode. Deze bevinden zich allemaal binnen de `api`-component. Er is geen actieve logging aangetroffen in de `omod` (web/UI-laag) of `acceptanceTest` componenten.

Hieronder volgt het volledige overzicht per bestand:

### A. Systeemactivatie & Configuratie
#### [RadiologyActivator.java](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/java/org/openmrs/module/radiology/RadiologyActivator.java)
* **Regel 26** `[INFO]`: `"Trying to start up Radiology Module"`
  * *Context:* Gelogd bij het initialiseren van de module door de OpenMRS core.
* **Regel 31** `[INFO]`: `"Radiology Module successfully started"`
  * *Context:* Gelogd na succesvolle opstart van de module.
* **Regel 41** `[INFO]`: `"PACS Configuration successfully loaded."`
  * *Context:* Bevestiging dat de PACS-omgevingsvariabelen geladen zijn.
* **Regel 42** `[INFO]`: `"PACS Host: " + pacsHost`
  * *Context:* Logt de geconfigureerde PACS hostnaam.
* **Regel 43** `[INFO]`: `"PACS WADO URL: " + pacsWadoUrl`
  * *Context:* Logt de geconfigureerde WADO endpoint URL.
* **Regel 45** `[WARN]`: `"PACS Environment variables are missing! Worklist push configurations might fail."`
  * *Context:* Waarschuwing wanneer de PACS netwerkconfiguratie ontbreekt in de omgevingsvariabelen.
* **Regel 51** `[INFO]`: `"Trying to shut down Radiology Module"`
  * *Context:* Gelogd bij het stoppen/deactiveren van de module.
* **Regel 56** `[INFO]`: `"Radiology Module successfully stopped"`
  * *Context:* Gelogd na succesvolle afsluiting.

### B. Orders en Workflows
#### [RadiologyOrderServiceImpl.java](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/java/org/openmrs/module/radiology/order/RadiologyOrderServiceImpl.java)
* **Regel 227** `[INFO]`: `"[RADIOLOGY] Order submitted: patientUuid=... orderUuid=... concept=... scheduledDate=... accessionNumber=..."`
  * *Context:* Technisch log-event bedoeld om ordersubmissies te loggen ten behoeve van DICOM worklists.
  * *Kritieke bevinding:* **Deze methode `logRadiologyOrderSubmission` wordt nergens in de code aangeroepen!** De functionaliteit is daardoor inactief.

### C. Systeemfouten & Data Parsing (Spring Property Editors)
Deze statements loggen uitsluitend fouten die optreden bij het binden en parsen van URL parameters naar Java objecten in de web-laag:
#### [RadiologyStudyEditor.java](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/java/org/openmrs/module/radiology/study/RadiologyStudyEditor.java)
* **Regel 49** `[ERROR]`: `"Error setting text: " + text` (inclusief stacktrace)
#### [RadiologyReportEditor.java](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/java/org/openmrs/module/radiology/report/RadiologyReportEditor.java)
* **Regel 49** `[ERROR]`: `"Error setting text: " + text` (inclusief stacktrace)
#### [MrrtReportTemplateEditor.java](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/java/org/openmrs/module/radiology/report/template/MrrtReportTemplateEditor.java)
* **Regel 51** `[ERROR]`: `"Error setting text: " + text` (inclusief stacktrace)
#### [RadiologyModalityEditor.java](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/java/org/openmrs/module/radiology/modality/RadiologyModalityEditor.java)
* **Regel 49** `[ERROR]`: `"Error setting text: " + text` (inclusief stacktrace)

### D. Report Templates & XML Validatie
#### [MrrtReportTemplateServiceImpl.java](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/java/org/openmrs/module/radiology/report/template/MrrtReportTemplateServiceImpl.java)
* **Regel 102** `[DEBUG]`: `"Tried to delete " + template.getPath() + " , but wasnt found."`
  * *Context:* Gelogd bij het verwijderen van een template-bestand dat fysiek niet meer op de disk aanwezig is.
#### [XsdMrrtReportTemplateValidator.java](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/java/org/openmrs/module/radiology/report/template/XsdMrrtReportTemplateValidator.java)
* **Regel 74, 80, 86** `[DEBUG]`: `exception.getMessage()` (inclusief stacktrace)
  * *Context:* Logt waarschuwingen en foutmeldingen tijdens XML schema-validatie (XSD) van MRRT templates.
* **Regel 94** `[ERROR]`: `e.getMessage()` (inclusief stacktrace)
  * *Context:* Logt SAX parser-initialisatiefouten.
#### [DefaultMrrtReportTemplateFileParser.java](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/java/org/openmrs/module/radiology/report/template/DefaultMrrtReportTemplateFileParser.java)
* **Regel 134** `[DEBUG]`: `"Unhandled meta tag " + name`
  * *Context:* Debug-informatie bij het parsen van MRRT templates.

---

## 3. Inventarisatielijst van Functionele Log-Events (Matrix)
NEN-7510 stelt dat kritieke gebruikersacties (zoals authenticatie, autorisatie, het raadplegen en muteren van patiëntgegevens) moeten worden vastgelegd in logs. 

Hieronder is weergegeven welke functionele acties momenteel daadwerkelijk een applicatielogregel genereren:

| Actie / Gebeurtenis | Type Actie | Genereert Applicatielog? | Status / Toelichting |
| :--- | :--- | :---: | :--- |
| **Inloggen / Uitloggen** | Beveiliging | **Nee** (Gedelegeerd) | Wordt afgehandeld door OpenMRS Core; de module logt dit zelf niet. |
| **Orders inzien (lezen)** | Patiëntgegevens | **Nee** | Geen enkele leesactie of zoekopdracht op orders genereert een logregel. |
| **Order aanmaken (schrijven)** | Patiëntgegevens | **Nee** (Kritiek lek/gap) | De methode `logRadiologyOrderSubmission` is gedefinieerd, maar wordt **nooit aangeroepen** in de service-laag. |
| **Order stopzetten / wijzigen** | Patiëntgegevens | **Nee** | Acties in `discontinueRadiologyOrder` genereren geen logregel. |
| **Rapport inzien (lezen)** | Patiëntgegevens | **Nee** | Het opvragen of downloaden van radiology-rapporten genereert geen logregel. |
| **Rapport aanmaken / opslaan** | Patiëntgegevens | **Nee** | Het aanmaken van concept- of definitieve rapporten genereert geen logregel. |
| **Rapport voiden (verwijderen)** | Patiëntgegevens | **Nee** | Het intrekken (voiden) van rapporten genereert geen logregel. |
| **Studie inzien (lezen)** | Patiëntgegevens | **Nee** | Geen logging op leesacties van `RadiologyStudy`. |
| **Studie aanmaken / opslaan** | Patiëntgegevens | **Nee** | Het aanmaken of koppelen van DICOM-studies genereert geen logregel. |
| **Modaliteiten beheren (CRUD)** | Administratief | **Nee** | Het toevoegen of bewerken van modalities (bijv. MRI, CT) genereert geen logregel. |
| **Templates beheren (CRUD)** | Administratief | **Gedeeltelijk** | Alleen fouten/debug-events bij het verwijderen van missende bestanden of XML-validatiefouten worden gelogd. Succesvolle creatie, wijziging of verwijdering wordt niet gelogd. |
| **Systeemfouten** | Systeem | **Ja** | Technische binding-errors (Property Editors) en XML parsing errors worden correct gelogd onder `ERROR`-niveau. |

> **Database-auditing versus Applicatie-logging:**
> OpenMRS Core legt via Hibernate interceptors automatisch metadata vast in de database bij mutaties (wie heeft een order/rapport aangemaakt of gewijzigd en wanneer). Dit voldoet aan de basiseis voor mutatie-auditing. Echter, **applicatie-logging** (het wegschrijven naar logbestanden voor SIEM/monitoring) en **lees-auditing** (wie heeft welke gegevens ingezien) ontbreken volledig.

---

## 4. Geïdentificeerde Gaps ten opzichte van NEN-7510

### Gap 1: Inactieve logging van klinische gebeurtenissen (Orders & Rapporten)
* **Omschrijving:** Er is geprobeerd om order-indieningen te loggen via `logRadiologyOrderSubmission` in [RadiologyOrderServiceImpl.java](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/api/src/main/java/org/openmrs/module/radiology/order/RadiologyOrderServiceImpl.java#L215). Echter, deze methode wordt nergens in de module aangeroepen. Er is daardoor **geen actieve logging** van klinische mutaties in de applicatielogs.
* **Risico:** Geen realtime detectie of auditing van geplaatste radiology-orders via externe loganalysetools (SIEM).

### Gap 2: Volledig gebrek aan 'Read Auditing' (Inzien van patiëntgegevens)
* **Omschrijving:** NEN-7510 vereist dat het inzien van medische dossiers en patiëntgegevens herleidbaar is. Binnen de module is er geen enkele vorm van logging wanneer een arts of medewerker orders inziat, rapporten opvraagt, of de radiology-dashboardtabbladen opent.
* **Risico:** Ongeautoriseerde inzage in patiëntgegevens (bijv. door nieuwsgierige medewerkers) kan achteraf niet worden gedetecteerd of gereconstrueerd via de logs.

### Gap 3: Geen logging van kritische administratieve wijzigingen
* **Omschrijving:** Wijzigingen in de systeemconfiguratie, zoals het aanpassen van modaliteiten (MRI, CT scanners) of het uploaden van MRRT rapportagetemplates, worden niet gelogd op applicatieniveau.
* **Risico:** Fouten in de configuratie of malafide configuratiewijzigingen kunnen niet worden getraceerd naar de verantwoordelijke gebruiker.

### Gap 4: Gebrek aan gestructureerd logformaat
* **Omschrijving:** De weinige logregels die aanwezig zijn (bijvoorbeeld bij het opstarten of bij foutmeldingen) zijn ongestructureerde tekstregels. Dit maakt het automatisch parsen en analyseren door een SIEM-systeem (zoals Splunk of Elastic Stack) lastig.
* **Risico:** Vertraagde detectie en respons bij beveiligingsincidenten.

---

## 5. Remediatieplan / Aanbevelingen

1. **Activeer en corrigeer de orderlogging:**
   Roep `logRadiologyOrderSubmission(radiologyOrder)` aan direct nadat een order succesvol is opgeslagen in `placeRadiologyOrder`. Zorg ervoor dat deze logregel (zoals reeds voorbereid) **geen direct herleidbare PII** (zoals namen of geboortedata) bevat, maar uitsluitend UUID's en het accession number om GDPR/AVG-compliance te borgen.

2. **Introduceer Read-logging voor Rapporten en Orders:**
   Voeg log-statements toe aan de controllers/endpoints die rapporten en orders ophalen (zoals `RadiologyReportFormController` en `RadiologyOrderDetailsPortletController`). Log ten minste:
   * Het UUID van de ingelogde gebruiker (`Context.getAuthenticatedUser().getUuid()`)
   * De actie (bijv. `"Viewed Radiology Report"`)
   * Het UUID van het geraadpleegde rapport/order en de patiënt.

3. **Log administratieve CRUD-acties:**
   Voeg info-logging toe aan de service-implementaties voor het beheren van modaliteiten en templates bij creatie, wijziging en verwijdering.

4. **Implementeer gestructureerde log-events:**
   Hanteer een eenduidig logformaat, bij voorkeur JSON-geformatteerd of met duidelijke key-value pairs (bijv. `event=order_placed user_uuid=... patient_uuid=...`), om integratie met security monitoring systemen te vergemakkelijken.
