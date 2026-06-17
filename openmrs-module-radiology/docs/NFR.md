# Meetbare non-functional requirements

## Doel en scope

Dit document beschrijft de meetbare non-functional requirements (NFR's) voor de OpenMRS Radiology-module. De eisen gelden voor nieuwe wijzigingen en releases van de `api`- en `omod`-modules. Ze maken kwaliteit controleerbaar op het gebied van onderhoudbaarheid, testen, performance, betrouwbaarheid, security en compatibiliteit.

Een NFR is pas aantoonbaar behaald wanneer de genoemde meetmethode bewijs oplevert. Een geslaagde handmatige beoordeling zonder rapport of testresultaat geldt niet als bewijs.

## Kwaliteitsnormen

| ID | Onderwerp | Meetbare norm | Meetmethode en bewijs | Controlemoment | Huidige borging |
|---|---|---|---|---|---|
| NFR-M01 | Cyclomatische complexiteit | Maximaal **10 per methode**. Een gemotiveerde uitzondering mag maximaal **15** zijn en moet in de pull request worden toegelicht. | SonarQube/SonarCloud-regel voor cyclomatische complexiteit; rapport per analyse. | Iedere pull request en push naar `main`. | Nog in te richten als quality gate. |
| NFR-M02 | Omvang methoden | Een Java-methode bevat maximaal **50 regels uitvoerbare code**. Gegenereerde code en testdata-builders zijn uitgezonderd. | Statische codeanalyse en review van gewijzigde code. | Iedere pull request. | Deels via review; automatische regel nog in te richten. |
| NFR-M03 | Code duplication | Maximaal **3% gedupliceerde regels** in nieuwe of gewijzigde Java-code. | SonarQube/SonarCloud duplicatiemeting op new code. | Iedere pull request. | Nog in te richten als quality gate. |
| NFR-M04 | Code style | **0 nieuwe Checkstyle-overtredingen** en alle broncode voldoet aan de repositoryformatter. | `mvn checkstyle:check` en `mvn formatter:validate` of een gelijkwaardige CI-controle. | Iedere pull request. | Checkstyle en formatter zijn geconfigureerd; blokkerende CI-controle moet worden bevestigd/ingericht. |
| NFR-T01 | Totale testdekking | Minimaal **75% line coverage** voor de gecombineerde productiecode van `api` en `omod`. | JaCoCo XML/HTML-rapport na `mvn clean test`; gewogen totaal op basis van covered en missed lines. | Iedere pull request en release. | JaCoCo-rapporten worden als CI-artifact opgeslagen; minimumgrens is nog niet blokkerend. |
| NFR-T02 | Kritieke testdekking | Minimaal **85% line coverage** voor order-, report-, study- en DICOM-service- en validatielogica in `api`. | JaCoCo package/class-rapport voor de betreffende packages. | Iedere pull request die kritieke zorglogica wijzigt en iedere release. | Rapportage aanwezig; minimumgrens nog niet blokkerend. |
| NFR-T03 | Dekking van gewijzigde code | Nieuwe of gewijzigde productiecode heeft minimaal **80% line coverage** en **70% branch coverage**. | SonarQube/SonarCloud new-code coverage of diff coverage uit JaCoCo. | Iedere pull request. | Nog in te richten. |
| NFR-T04 | Testsuite | **100% van de geautomatiseerde unit- en componenttests slaagt**; er zijn geen genegeerde tests zonder gekoppeld issue en motivatie. | Resultaat van `mvn clean test -B` in GitHub Actions. | Iedere pull request en push naar `main`. | Aanwezig in `ci-cd.yml`. |
| NFR-P01 | Leesperformance | Zoeken en openen van radiologieorders en -rapporten heeft een **p95-responstijd van maximaal 2 seconden** bij 20 gelijktijdige gebruikers en een dataset van minimaal 10.000 orders. | Herhaalbare loadtest tegen de testomgeving; rapport met p50, p95, foutpercentage, dataset en commit-SHA. | Voor iedere release en na wijzigingen aan queries of zoekfunctionaliteit. | Loadtest nog in te richten. |
| NFR-P02 | Schrijfperformance | Aanmaken of wijzigen van een order of rapport heeft een **p95-responstijd van maximaal 3 seconden** bij 20 gelijktijdige gebruikers. | Dezelfde loadtestcondities als NFR-P01. Externe PACS-verwerking valt buiten de gemeten requesttijd. | Voor iedere release en na wijzigingen aan deze workflows. | Loadtest nog in te richten. |
| NFR-P03 | Stabiliteit onder belasting | Tijdens een loadtest van **30 minuten** blijft het HTTP-foutpercentage onder **1%** en ontstaan geen `OutOfMemoryError` of ongecontroleerde applicatiecrashes. | Loadtestrapport en applicatielogs uit de testomgeving. | Voor iedere release. | Nog in te richten. |
| NFR-R01 | Functionele betrouwbaarheid | Een mislukte schrijfoperatie mag geen gedeeltelijke order-, study- of reportstatus opslaan; **100% van de rollback-integratietests slaagt**. | Component-/integratietests die een fout forceren en daarna de database-status controleren. | Iedere pull request die transactielogica wijzigt. | Bestaande tests bieden gedeeltelijke dekking; expliciete rollbacktests waar nodig toevoegen. |
| NFR-R02 | Beschikbaarheid | De module veroorzaakt in productie maximaal **43 minuten ongeplande uitval per kalendermaand** (**99,9% beschikbaarheid**), exclusief aangekondigd onderhoud en uitval van externe OpenMRS/PACS-systemen. | Uptime-monitoring op een health endpoint, maandelijks beschikbaarheidsrapport en incidentregistratie. | Continu en maandelijks gerapporteerd. | Operationele monitoring valt buiten de huidige CI en moet per deployment worden ingericht. |
| NFR-S01 | Bekende kwetsbaarheden | Bij een release zijn er **0 openstaande Critical of High** dependency- of CodeQL-bevindingen. Een uitzondering vereist schriftelijke risicoacceptatie met eigenaar en einddatum. | GitHub CodeQL, Dependabot en SBOM/SCA-rapport gekoppeld aan de release. | Iedere pull request en release. | CodeQL, Dependabot en SBOM-workflows zijn aanwezig. |
| NFR-S02 | Oplostermijn kwetsbaarheden | Critical: binnen **48 uur** beoordeeld en binnen **7 dagen** opgelost; High: **14 dagen**; Medium: **30 dagen**; Low: **90 dagen**. | GitHub Security-alerts/issues met detectiedatum, severity, eigenaar en sluitingsdatum. | Wekelijkse securitytriage. | Procesmatig te borgen. |
| NFR-S03 | Bescherming persoonsgegevens | **0 patiëntidentificerende waarden** in applicatie- en auditlogs, waaronder naam, adres, geboortedatum, patient identifier, vrije verslagtekst en volledige DICOM-metadata. | Geautomatiseerde logtests plus steekproef van testomgevingslogs met synthetische herkenbare waarden. | Iedere pull request die logging wijzigt en voor iedere release. | `LogUtils` en logtests zijn aanwezig; dekking per gewijzigd logpad controleren. |
| NFR-S04 | Autorisatie | **100% van de REST- en controller-acties** die orders, studies of rapporten lezen of wijzigen heeft een positieve en negatieve autorisatietest. Een gebruiker zonder vereiste privilege krijgt HTTP **401 of 403** en er vindt geen mutatie plaats. | Geautomatiseerde controller-/REST-integratietests. | Iedere pull request die een endpoint of privilege wijzigt. | Per endpoint te inventariseren en waar nodig aan te vullen. |
| NFR-C01 | Platformcompatibiliteit | De module compileert en alle tests slagen met **JDK 8** en **OpenMRS Platform 2.8.7**. | Schone CI-build met de versies uit de parent `pom.xml`. | Iedere pull request en release. | Aanwezig in Maven-configuratie en CI. |
| NFR-C02 | Databasewijzigingen | **100% van de Liquibase changesets** kan op een lege ondersteunde database worden uitgevoerd en een bestaande database kan zonder handmatige datacorrectie worden bijgewerkt. | Geautomatiseerde installatietest en upgradetest op een kopie van representatieve testdata. | Bij iedere databasewijziging en voor iedere release. | Nog als vaste releasecontrole in te richten. |
| NFR-O01 | Buildtijd | De standaard CI-job voor compilatie en JUnit-tests voltooit in **maximaal 10 minuten** bij minimaal 95% van de runs. | GitHub Actions-duur over de laatste 20 geslaagde runs. | Maandelijks en voor iedere release. | Meetbaar via GitHub Actions; rapportage nog procesmatig te borgen. |

## Vaste meetcondities voor performance

Performancecijfers zijn alleen vergelijkbaar als minimaal de volgende gegevens in het rapport staan:

- commit-SHA en moduleversie;
- OpenMRS-versie, Java-versie, databaseversie en gebruikte infrastructuur;
- minimaal 10.000 radiologieorders, inclusief studies en rapporten;
- een opwarmperiode van 5 minuten, gevolgd door een meetperiode van minimaal 30 minuten;
- 20 gelijktijdige virtuele gebruikers met een realistische mix van lees- en schrijfacties;
- p50- en p95-responstijd, throughput en HTTP-foutpercentage.

Een performancevergelijking is ongeldig wanneer de omgeving of dataset wezenlijk afwijkt zonder dat dit in het rapport is vermeld.

## Quality gate voor samenvoegen

Een pull request mag worden samengevoegd wanneer alle voor de wijziging relevante eisen slagen. Minimaal gelden altijd:

1. de build en alle unit- en componenttests slagen;
2. er zijn geen nieuwe Critical of High securitybevindingen;
3. gewijzigde code introduceert geen ongemotiveerde overschrijding van de complexiteitsnorm;
4. de totale coverage daalt niet onder 75% en gewijzigde code voldoet aan NFR-T03;
5. afwijkingen zijn vastgelegd met reden, risico, eigenaar en een concrete einddatum.

## Bewijs bij een release

Het releasebewijs bestaat minimaal uit:

- de geslaagde GitHub Actions-run;
- de JaCoCo-rapporten voor `api` en `omod`;
- het CodeQL-, Dependabot- en SBOM/SCA-resultaat;
- het meest recente performancerapport;
- een overzicht van tijdelijke NFR-afwijkingen en hun einddatum.

## Implementatieprioriteit

De normen in dit document zijn het doel; niet alle quality gates zijn momenteel technisch afgedwongen. De aanbevolen volgorde voor verdere automatisering is:

1. JaCoCo `check`-regels toevoegen voor de minimale coverage;
2. SonarQube/SonarCloud quality gates instellen voor complexiteit, duplicatie en new-code coverage;
3. Checkstyle en formattervalidatie blokkerend maken in CI;
4. een reproduceerbare loadtest toevoegen;
5. autorisatie-, rollback- en database-upgradetests aanvullen.
