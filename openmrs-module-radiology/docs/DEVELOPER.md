# DEVELOPER GUIDE

#### Table of Contents

1. [Overview](#overview)
2. [Build](#build)
  * [Plugins](#plugins)
    * [License Header](#license-header)
    * [Formatter](#formatter)
    * [Code Style](#code-style)
    * [Test coverage](#test-coverage)
    * [Docker](#docker)
3. [Tests](#tests)
  * [Overview](#test-overview)
  * [Running tests locally](#running-tests-locally)
  * [Test inventory](#test-inventory)
  * [Gap analysis and backlog](#gap-analysis-and-backlog)
4. [Non-functional requirements](NFR.md)

## Overview

Deze gids biedt elke ontwikkelaar een snelle referentie voor nuttige commando's en richtlijnen die binnen deze module worden gebruikt.

Dit document is een werk in uitvoering :construction_worker:

Bijdragen zijn welkom!

## Build

De gebruikte build-tool is [Maven](https://maven.apache.org/)

### Plugins

#### License Header

We gebruiken http://code.mycila.com/license-maven-plugin/ om te controleren of elk Java-, XML- en TXT-bestand een correcte OpenMRS-licentieheader bevat. De inhoud van de licentieheader wordt overgenomen uit `license-header.txt`. De opmaak hangt af van het bestandstype (Java, XML, TXT).

Deze plugin wordt automatisch uitgevoerd bij `mvn clean package/install` en faalt op GitHub Actions / Travis CI als een bestand een correcte licentieheader mist.

##### Commands

_Licentieheader toevoegen of bijwerken_

```bash
mvn license:format
```

_Controleren op foutieve of ontbrekende licentieheaders_

```bash
mvn license:check
```

#### Formatter

We gebruiken https://github.com/velo/maven-formatter-plugin als broncode-formatter.

Deze plugin wordt automatisch uitgevoerd bij `mvn clean package/install` (niet in de CI-pipeline) en formatteert alleen Java- en JavaScript-bestanden. JSP's en XML-bestanden moet je zelf formatteren, dus configureer de formatter van je IDE correct.

Zie de [formatter gids](FORMATTER.md) voor het configureren van je IDE.

##### Commands

_Broncode formatteren_

```bash
mvn formatter:format
```

#### Code Style

We hebben een aantal regels over wat we beschouwen als code met stijl :bowtie:

Je vindt de regels voor Java-code in `checkstyle.xml`.

Dit bestand is geïmporteerd in [Codacy](https://www.codacy.com/app/teleivo/openmrs-module-radiology_2/dashboard), waarmee we publiekelijk inzichtelijk maken aan welke verbeterpunten we moeten werken.

##### Commands

_Broncode controleren op stijlrichtlijnen_

Als je jouw wijzigingen lokaal wilt controleren, voer dan uit:

```bash
mvn checkstyle:checkstyle
```

Dit genereert HTML-rapporten in:

* `api/target/site/checkstyle.html`
* `omod/target/site/checkstyle.html`

#### Test coverage

JaCoCo is geconfigureerd in de parent `pom.xml`. Na het uitvoeren van de tests worden dekkingsrapporten gegenereerd in `api/target/site/jacoco/` en `omod/target/site/jacoco/`.

```bash
mvn clean test
```

##### Code Coverage Norm & Onderbouwing

Binnen de Radiologie-module hanteren we een **target code coverage van minimaal 75%** voor de gecombineerde codebase. 

De volledige set meetbare kwaliteitsnormen, inclusief cyclomatische complexiteit, coverage, performance, betrouwbaarheid en security, staat in [Meetbare non-functional requirements](NFR.md).

###### Waarom geen 100%?
Blind streven naar 100% code coverage is niet effectief en leidt tot inefficiënt gebruik van ontwikkeltijd. Het dwingt ontwikkelaars om triviale onderdelen te testen (zoals getters/setters, constante-klassen zoals `RadiologyWebConstants`, configuration mappings en boilerplate code) of om complexe OpenMRS Core libraries overmatig te mocken. Dit verhoogt de onderhoudslast van de testsuite aanzienlijk, zonder dat het daadwerkelijk kritieke bugs voorkomt.

###### Risico-gebaseerde prioritering (Context van de module)
Onze teststrategie richt zich daarom op risico en bedrijfskritische functionaliteit:

1. **High-Risk Paden (Kritieke zorg-functionaliteit): Target > 85%**
   * *Wat valt hieronder:* De core services en validatieregels in de `api`-module, zoals `RadiologyOrderService` (aanmaken en stopzetten van radiology orders), `RadiologyReportService` (opstellen en afronden van diagnoses/verslagen), en `RadiologyStudyService` (koppelen van DICOM metadata).
   * *Onderbouwing:* Fouten in deze kritieke klinische workflows kunnen direct impact hebben op de patiëntveiligheid (bijv. verkeerd gekoppelde beelden of verloren verslagen). Deze functionaliteiten moeten daarom robuust gedekt zijn met unit- en componenttests.
   
2. **Low-Risk / Out-of-Scope Paden (Legacy UI & Boilerplate): Target ~ 60%**
   * *Wat valt hieronder:* De Spring MVC controllers en legacy JSP-schermen (`omod`-submodule) voor het oude OpenMRS dashboard.
   * *Onderbouwing:* Deze legacy UI-code is op termijn genomineerd voor uitfasering ten gunste van modernere frontend-frameworks (zoals de OpenMRS 3.x microfrontends). Het schrijven van complexe integratietests voor Spring-controllers die gekoppeld zijn aan verouderde JSP-technologie levert weinig ROI op. Voor deze laag vertrouwen we primair op basale REST-resource tests en end-to-end acceptatietests (`acceptanceTest/`).

#### Docker

Lees de bijbehorende [Docker gids](DOCKER.md).

## Tests

### Test overview

De Radiologie-module heeft vier testlagen:

| Layer | Location | Framework | Count (approx.) |
|-------|----------|-----------|-----------------|
| Backend unit & component tests | `api/src/test` | JUnit 4 + OpenMRS test framework | 33 klassen, ~372 testmethoden |
| Web & REST tests | `omod/src/test` | JUnit 4 + OpenMRS test framework | 26 klassen, ~184 testmethoden |
| Frontend unit tests | `omod/src/test/javascript` | Jasmine (via `jasmine-maven-plugin`) | 1 spec-bestand, 6 testcases |
| Acceptance / E2E tests | `acceptanceTest/` | Gradle + Geb/Spock + Selenium | 2 E2E specs + 2 DB-tests |

**Totaal aantal JUnit-tests: ~556** (lokaal geverifieerd met `mvn test` — allemaal succesvol).

JUnit-tests worden automatisch uitgevoerd in de CI-pipeline (zie `.github/workflows/ci-cd.yml`). Acceptatietests vereisen een draaiende OpenMRS-instantie met database-toegang en maken momenteel nog geen deel uit van de CI.

### Running tests locally

#### Prerequisites

- JDK 8
- Maven 3.x
- Voor acceptatietests: een geconfigureerde OpenMRS-instantie, MySQL-toegang en Gradle

#### All JUnit tests (API + OMOD)

Vanaf de module root (`openmrs-module-radiology/`):

```bash
mvn test
```

Voer alleen de API- of OMOD-module uit:

```bash
mvn test -pl api
mvn test -pl omod
```

Een specifieke testklasse uitvoeren:

```bash
mvn test -pl api -Dtest=RadiologyOrderServiceComponentTest
mvn test -pl omod -Dtest=RadiologyOrderFormControllerTest
```

#### Jasmine frontend tests

De Jasmine-plugin is geconfigureerd in `omod/pom.xml` maar is niet gekoppeld aan de standaard test-fase. Voer deze expliciet uit:

```bash
mvn jasmine:test -pl omod
```

Dit vereist PhantomJS (wordt automatisch gedownload door de plugin).

#### Acceptance tests

Zie [acceptanceTest/readme.md](../acceptanceTest/readme.md) voor de volledige installatie-instructies.

Snel starten (vereist een geconfigureerde OpenMRS + database):

```bash
cd acceptanceTest
# Eerste keer opstarten: installeer de Gradle-plugin
cd plugin/mUpdate && gradle install && cd ../..
# Voer tests uit in Chrome
gradle chromeTest -DconfigLoginFile="config/login/config_login_example.config" -DconfigDBFile="config/db/config_db_example.config"
```

Andere Gradle-taken: `gradle phantomJsTest`, `gradle test` (alle browsers), `gradle updateModule` (de nieuwste OMOD uitrollen).

#### Test coverage report

```bash
mvn clean test jacoco:report
```

Rapporten: `api/target/site/jacoco/index.html`, `omod/target/site/jacoco/index.html`.

### Test inventory

#### API module (`api/src/test/java`)

| Package / domain | Test class | Type | What is tested |
|------------------|-----------|------|----------------|
| **dicom** | `PerformedProcedureStepStatusTest` | Unit | DICOM-statuscode enum |
| | `DicomUidValidatorTest` | Unit | DICOM UID-validatieregels |
| | `DicomWebViewerTest` | Mock | WADO-viewer URL-constructie |
| | `UuidDicomUidGeneratorTest` | Unit | Genereren van UUID-naar-DICOM-UID |
| **modality** | `RadiologyModalityValidatorTest` | Component | Modaliteit veldvalidatie |
| | `RadiologyModalityServiceComponentTest` | Component | Modaliteit CRUD-service |
| | `RadiologyModalityEditorComponentTest` | Component | Modaliteit property-editor |
| **order** | `RadiologyOrderTest` | Unit | Gedrag van domeinobject |
| | `RadiologyOrderValidatorTest` | Unit | Order-validatieregels |
| | `RadiologyOrderSearchCriteriaTest` | Unit | Zoekcriteria-builder |
| | `RadiologyOrderServiceTest` | Mock | Service-interface (gemockt) |
| | `RadiologyOrderServiceComponentTest` | Component | Volledige levenscyclus van orders (plaatsen, stopzetten, ondertekenen) |
| | `RadiologyOrderServiceImplComponentTest` | Component | Specifieke details van service-implementatie |
| | `HibernateRadiologyOrderDAOComponentTest` | Component | Order DAO / Hibernate-query's |
| **report** | `RadiologyReportTest` | Unit | Gedrag van domeinobject |
| | `RadiologyReportValidatorTest` | Unit | Rapport-validatieregels |
| | `RadiologyReportSearchCriteriaTest` | Unit | Zoekcriteria-builder |
| | `RadiologyReportServiceTest` | Mock | Service-interface (gemockt) |
| | `RadiologyReportServiceComponentTest` | Component | Rapport CRUD, intrekken (void), claimen |
| | `RadiologyReportEditorComponentTest` | Component | Rapport property-editor |
| **report/template** | `MrrtReportTemplateServiceComponentTest` | Component | Sjabloon importeren, zoeken, archiveren (retire) |
| | `MrrtReportTemplateValidatorComponentTest` | Component | MRRT-sjabloonvalidatie |
| | `MrrtReportTemplateFileParserComponentTest` | Component | HTML-sjabloonbestand parseren |
| | `MrrtReportTemplateEditorComponentTest` | Component | Sjabloon property-editor |
| | `MrrtReportTemplateSearchCriteriaTest` | Unit | Zoekcriteria-builder |
| | `MetaTagsValidationEngineTest` | Unit | MRRT meta-tag validatie-engine |
| | `ValidationResultTest` | Unit | Aggregatie van validatieresultaten |
| **study** | `RadiologyStudyTest` | Unit | Gedrag van domeinobject |
| | `RadiologyStudyServiceComponentTest` | Component | Studie CRUD-service |
| | `RadiologyStudyServiceImplTest` | Mock | Service-implementatie (gemockt) |
| | `RadiologyStudyEditorComponentTest` | Component | Studie property-editor |
| **util** | `DecimalUuidTest` | Unit | Decimale UUID-hulpprogramma (utility) |
| **config** | `RadiologyPropertiesComponentTest` | Component | Globale eigenschappen / configuratie |

#### OMOD module (`omod/src/test/java`)

| Package / domain | Test class | Type | What is tested |
|------------------|-----------|------|----------------|
| **order/web** | `RadiologyOrderFormControllerTest` | Mock | Orderformulier-controller |
| | `RadiologyOrderDetailsPortletControllerTest` | Mock | Orderdetails portlet-controller |
| | `RadiologyDashboardOrdersTabControllerTest` | Unit | Dashboard ordertab-controller |
| | `DiscontinuationOrderRequestValidatorTest` | Unit | Validatie van stopzettingsverzoek |
| | `RadiologyOrderSearchHandlerTest` | Unit | REST zoekafhandeling-logica |
| | `RadiologyOrderSearchHandlerComponentTest` | Component | REST zoekafhandeling-integratie |
| | `RadiologyOrderResourceTest` | Unit | REST resource-representatie |
| | `RadiologyOrderResourceComponentTest` | Component | REST resource CRUD |
| | `RadiologyOrderControllerComponentTest` | Component | REST-controller voor orders |
| **modality/web** | `RadiologyModalityFormControllerTest` | Mock | Modaliteitformulier-controller |
| | `RadiologyDashboardModalitiesTabControllerTest` | Unit | Dashboard modaliteitentab-controller |
| | `RadiologyModalityResourceTest` | Unit | REST resource-representatie |
| | `RadiologyModalityResourceComponentTest` | Component | REST resource CRUD |
| **report/web** | `RadiologyReportFormControllerTest` | Mock | Rapportformulier-controller |
| | `RadiologyDashboardReportsTabControllerTest` | Unit | Dashboard rapportentab-controller |
| | `VoidRadiologyReportRequestValidatorTest` | Unit | Validatie van intrekkingsverzoek (void) |
| | `RadiologyReportResourceTest` | Unit | REST resource-representatie |
| | `RadiologyReportResourceComponentTest` | Component | REST resource CRUD |
| | `RadiologyReportControllerComponentTest` | Component | REST-controller voor rapporten |
| | `RadiologyReportSearchHandlerComponentTest` | Component | REST zoekafhandeling-integratie |
| **report/template/web** | `MrrtReportTemplateFormControllerTest` | Mock | Sjabloonformulier-controller |
| | `RadiologyDashboardReportTemplatesTabControllerTest` | Mock | Dashboard sjablonentab-controller |
| | `MrrtReportTemplateResourceTest` | Unit | REST resource-representatie |
| | `MrrtReportTemplateResourceComponentTest` | Component | REST resource CRUD |
| | `MrrtReportTemplateSearchHandlerTest` | Unit | REST zoekafhandeling-logica |
| | `MrrtReportTemplateSearchHandlerComponentTest` | Component | REST zoekafhandeling-integratie |

#### Jasmine frontend (`omod/src/test/javascript`)

| Spec file | What is tested |
|-----------|----------------|
| `radiologySpec.js` | Hulpfuncties `Radiology.getRestRootEndpoint()` en `Radiology.getProperty()` |

#### Acceptance tests (`acceptanceTest/`)

| Test | Framework | What is tested |
|------|-----------|----------------|
| `LoginSpec.groovy` | Geb/Spock | In- en uitlogflow |
| `RadiologyOrdersReportSpec.groovy` | Geb/Spock | Orders inzien, rapport aanmaken, rapport ondertekenen (volledige E2E) |
| `FindUsersTest.java` | JUnit | Database testdata-setup (bestaan van patiënt/zorgverlener) |
| `ModuleUpdaterPluginTest.groovy` | Spock | Gradle module-updater plugin |
| `ModuleUpdaterTaskTest.groovy` | Spock | Gradle module-updater taak |

### Gap analysis and backlog

De onderstaande tabel toont de klassen en scenario's die momenteel nog specifieke testdekking missen. De prioriteit is gebaseerd op impact op de bedrijfsvoering en het risico op regressie.

#### High priority

| ID | Class / area | Suggested test | Module |
|----|-------------|----------------|--------|
| T-01 | `HibernateRadiologyReportDAO` | Componenttest voor rapportagequery's (zoeken, intrekken, claimen) | api |
| T-02 | `HibernateRadiologyStudyDAO` | Componenttest voor persistentie en het opzoeken van studies | api |
| T-03 | `HibernateRadiologyModalityDAO` | Componenttest voor modaliteitsquery's | api |
| T-04 | `HibernateMrrtReportTemplateDAO` | Componenttest voor het zoeken en importeren van sjablonen | api |
| T-05 | `XsdMrrtReportTemplateValidator` | Unittest met valide/invalide MRRT XSD-sjablonen | api |
| T-06 | `AccessionNumberGenerator` | Unittest om de uniekheid en thread-safety te verifiëren | api |
| T-07 | `RadiologyRestController` | Specifieke unittest voor REST endpoint-routing | omod |
| T-08 | Acceptance: modality management | E2E-spec voor het aanmaken/bewerken van modaliteiten | acceptanceTest |
| T-09 | Acceptance: template management | E2E-spec voor het importeren van MRRT-sjablonen | acceptanceTest |

#### Medium priority

| ID | Class / area | Suggested test | Module |
|----|-------------|----------------|--------|
| T-10 | `ElementsExpressionValidationRule` | Unittest voor XPath-expressievalidatie | api |
| T-11 | `RadiologyActivator` | Componenttest voor de registratie van rechten bij het opstarten | api |
| T-12 | `PatientDashboardRadiologyTabExt` | Unittest voor de zichtbaarheidsvoorwaarden van de extensie | omod |
| T-13 | `GutterListExt` | Unittest voor menu-items in de admin-kolom | omod |
| T-14 | `AdminList` | Unittest voor menu-items op de admin-pagina | omod |
| T-15 | Jasmine: `radiology.js` | Specs uitbreiden voor de overige JavaScript-hulpfuncties | omod |
| T-16 | Acceptance: order discontinuation | E2E-spec voor het stopzetten van radiologie-orders | acceptanceTest |
| T-17 | Acceptance: DICOM viewer link | E2E-spec ter verificatie van de WADO-viewer URL in de orderdetails | acceptanceTest |

#### Low priority

| ID | Class / area | Suggested test | Module |
|----|-------------|----------------|--------|
| T-18 | `RadiologyPrivileges` | Unittest ter verificatie van de rechtenconstanten | api |
| T-19 | `RadiologyReportStatus` | Unittest voor de statusovergangen binnen de enum | api |
| T-20 | `RadiologyWebConstants` | Unittest voor constante waarden | omod |
| T-21 | CI: acceptance tests | Integreren van de Gradle-acceptatietests in de CI-pipeline (vereist een testomgeving) | ci |
| T-22 | CI: Jasmine tests | `jasmine:test` koppelen aan de standaard Maven test-fase of als aparte CI-stap toevoegen | ci |

#### Coverage strengths

De volgende domeinen zijn al uitstekend gedekt en dienen als referentiemateriaal voor het schrijven van nieuwe tests:

- **Radiology orders**: Volledige levenscyclus (plaatsen → stopzetten → ondertekenen) via componenttests
- **Radiology reports**: CRUD, intrekken (void), claimen en validatie
- **MRRT templates**: Importeren, valideren, zoeken en blootstelling via REST
- **REST API**: Resource-representaties en zoekafhandelingen voor orders, rapporten, modaliteiten en sjablonen
- **Web controllers**: Formulier-controllers en dashboard-tab-controllers (op basis van mocks)
