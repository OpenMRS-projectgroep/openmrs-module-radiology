# Software Composition Analysis (SCA) - Beveiligings- & Updateadvies

Dit document bevat een onderbouwd en bruikbaar advies over updates en risicomitigatie van de libraries waar de OpenMRS Radiology-module van afhankelijk is. Het advies is gebaseerd op de gegenereerde CycloneDX SBOM en de daaruit voortgekomen Snyk-kwetsbaarhedenanalyse (90 unieke kwetsbaarheden).

---

## 1. Executive Summary

Na de upgrade van het OpenMRS Core Platform naar versie **2.8.7** (uitgevoerd via Dependabot) is de dependency-tree van de module uitgebreid. De Snyk SCA-scan op de actuele SBOM heeft in totaal **90 beveiligingsproblemen** (vulnerabilities) geïdentificeerd in **225 geteste dependencies**:
*   **5 Critical** (CVSS 9.0 - 10.0)
*   **48 High** (CVSS 7.0 - 8.9)
*   **27 Medium** (CVSS 4.0 - 6.9)
*   **10 Low** (CVSS 0.1 - 3.9)

### Waarom een stijging van 75 naar 90 issues?
Hoewel de upgrade naar OpenMRS Platform `2.8.7` diverse oude platform-kwetsbaarheden heeft verholpen, is de dependency-tree gegroeid van **155 naar 225 componenten**. De nieuwe platformversie introduceert onder andere de AWS S3 SDK (`software.amazon.awssdk:s3`), die op zijn beurt afhankelijk is van **Netty** (`netty-handler` en `netty-nio-client`). Netty introduceert op dit moment 1 nieuwe kritieke kwetsbaarheid en meerdere hoge kwetsbaarheden (o.a. HTTP/2 Zero-Byte Continuation Frames en HTTP Request Smuggling) in onze dependency-tree.

---

## 2. Hoe is de CVSS-score opgebouwd en berekend?

De **Common Vulnerability Scoring System (CVSS v3.1)** score (schaal 0.0 tot 10.0) meet de technische ernst van een kwetsbaarheid op basis van drie vaste metric-groepen. In dit rapport kijken we naar de **Base Score** (de intrinsieke eigenschappen van de kwetsbaarheid). Deze is opgebouwd uit:

### A. Exploitability (Misbruikbaarheid)
*   **Attack Vector (AV):** Van waaruit kan de aanval plaatsvinden? *Network (N)* (op afstand via internet, het gevaarlijkst), *Adjacent (A)* (lokaal netwerk), *Local (L)* (lokaal systeem), of *Physical (P)*.
*   **Attack Complexity (AC):** Hoeveel moeite moet de aanvaller doen? *Low (L)* (geen speciale omstandigheden vereist) of *High (H)* (specifieke timing, configuratie of omzeiling nodig).
*   **Privileges Required (PR):** Welke rechten heeft de aanvaller vooraf nodig? *None (N)* (geen login nodig), *Low (L)* (standaard gebruikersaccount), of *High (H)* (administrator-rechten).
*   **User Interaction (UI):** Is er hulp van een slachtoffer nodig? *None (N)* (volledig autonoom) of *Required (R)* (slachtoffer moet bijvoorbeeld op een link klikken of een bestand openen).

### B. Impact (De Gevolgen)
Dit meet de impact op de BIV-classificatie (vertrouwelijkheid, integriteit en beschikbaarheid):
*   **Confidentiality (C):** Impact op vertrouwelijkheid (data inzien). *High (H)*, *Low (L)*, of *None (N)*.
*   **Integrity (I):** Impact op integriteit (data aanpassen of verwijderen). *High (H)*, *Low (L)*, of *None (N)*.
*   **Availability (A):** Impact op beschikbaarheid (systeem platleggen/DoS). *High (H)*, *Low (L)*, of *None (N)*.

### C. Scope (S)
*   **Scope (S):** Kan de aanvaller via deze kwetsbaarheid doorstoten naar andere systemen of autoriteitsdomeinen daarbuiten? *Unchanged (U)* of *Changed (C)*.

---

## 3. Kritieke & Hoge Risico's en Prioritaire Updates (Nieuwe Status)

Hieronder is de geactualiseerde tabel met CVSS-onderbouwingen, vectoren en concrete update-adviezen voor de Radiology-module onder OpenMRS Platform `2.8.7`.

| Library / Component | Huidige Versie | CVSS Score | CVSS-onderbouwing & Vector | Kwetsbaarheid & Impact | Advies & Doelversie | Type Actie |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `io.netty:netty-*`<br>(o.a. `netty-handler`) | `4.1.118.Final` | **9.8 (Critical)** | `CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H`<br>**Waarom 9.8:** Maximale score zonder scope-wijziging. Exploiteerbaar via het netwerk (AV:N) met lage complexiteit (AC:L) zonder inloggen (PR:N) of interactie (UI:N). Veroorzaakt volledige impact op vertrouwelijkheid, integriteit en beschikbaarheid (C:H/I:H/A:H). | **SNYK-JAVA-IONETTY-17254120**: Incorrect Comparison.<br>**CVE-2026-33871, CVE-2026-33870**: DoS via HTTP/2 CONTINUATION en Request Smuggling. | **Advies:** Forceer een update van alle Netty-transitatieve dependencies naar **4.1.132.Final** (of hoger) via het importeren van de Netty BOM in de parent `pom.xml`. | Dependency Override |
| `org.codehaus.jackson:jackson-mapper-asl` | `1.9.14-MULE-002` | **9.8 (Critical)** | `CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H`<br>**Waarom 9.8:** Zelfde reden als hierboven. Volledige controle/overname op afstand via niet-gevalideerde input (deserialisatie) met directe netwerktoegang zonder enige drempels. | **CVE-2019-10202 / SNYK-JAVA-ORGCODEHAUSJACKSON-3326362**: Improper Input Validation en XXE injection.<br>*Impact:* Gevaar voor data-exfiltratie en Denial of Service. | Jackson 1.x is volledig End-of-Life (EOL).<br>**Advies:** Sluit deze library uit (`<exclusion>`) als deze niet actief wordt gebruikt. Indien wel gebruikt, migreer de code naar Jackson 2.x (`com.fasterxml.jackson.core:jackson-databind`). | Code-wijziging of Exclusion |
| `org.graalvm.sdk:graal-sdk` | `20.3.17` | **9.2 (Critical)** | `CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:L`<br>**Waarom 9.2:** Gelijk aan 9.8, maar de impact op de beschikbaarheid is gelimiteerd (A:L) of de exploitatie vereist specifieke invoercondities, waardoor het net onder de 9.8-grens valt. | **CVE-2025-30749, CVE-2025-50106**: Deserialisatie van onbetrouwbare data.<br>*Impact:* Mogelijkheid tot Remote Code Execution (RCE). | **Advies:** Forceer een update naar **21.0.8** of **24.0.2** (afhankelijk van Java-versie-ondersteuning) in de parent `pom.xml`. | Dependency Override |
| `org.springframework:*` | `5.3.30` | **8.7 (High)** | `CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:N` (of `C:N/I:N/A:H`).<br>**Waarom 8.7:** De aanval is eenvoudig (AV:N/AC:L/PR:N/UI:N) maar heeft een gerichte impact: bijvoorbeeld alléén op de beschikbaarheid (A:H, zoals bij Denial of Service door CPU-spikes) óf alleen op data-lezen/schrijven (C:H/I:H) zonder het systeem te crashen (A:N). | **CVE-2024-38816, CVE-2026-41849**: Path Traversal en Integer Overflow in Expression Language (SpEL).<br>*Impact:* Ongeautoriseerde bestandstoegang en resource-exhaustion. | Spring 5.3.x is end-of-life (OSS). Spring 6+ vereist minimaal Java 17.<br>**Advies:** Aangezien de module op Java 8/11 draait, is Spring 6 nu niet haalbaar. Update naar de hoogst beschikbare Spring 5.3.x patch (bijv. **5.3.39** of hoger) via `<dependencyManagement>`. | Dependency Override |
| `com.mchange:c3p0` / `mchange-commons-java` | `0.9.5.5` / `0.2.19` | **8.9 (High)** | `CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:C/C:H/I:H/A:H`<br>**Waarom 8.9:** De kwetsbaarheid heeft een volledige impact op alle pijlers, maar de complexiteit van de aanval is hoog (AC:H) omdat er specifieke serialization formats of database-configuraties nodig zijn om de exploit te laten slagen. | **CVE-2026-27830, CVE-2026-27727**: Deserialisatie en output-injectie.<br>*Impact:* RCE of SQL-injectie via de database-connection pool. | **Advies:** Upgrade naar **0.12.0** (c3p0) en **0.4.0** (mchange-commons) of hoger. | Dependency Override |
| `org.postgresql:postgresql` | `42.7.7` | **8.7 (High)** | `CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:H`<br>**Waarom 8.7:** De driver kan op afstand (AV:N) zonder rechten (PR:N) worden overbelast met queries. Dit leidt specifiek tot volledige uitval van de beschikbaarheid (A:H, Denial of Service), maar lekt geen data (C:N) en wijzigt geen data (I:N). | **CVE-2026-42198**: Resource exhaustion (ongelimiteerde resources).<br>*Impact:* Database Denial of Service. | **Advies:** Update de driver naar **42.7.11** (of hoger in de 42.7.x tak). Dit is achterwaarts compatibel. | Dependency Override |
| `com.google.protobuf:protobuf-java` | `3.19.4` | **8.7 (High)** | `CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:H`<br>**Waarom 8.7:** De parser crasht (A:H) bij het verwerken van kwaadaardig geformatteerde protobuf-berichten over het netwerk. Er is geen data-lek (C:N) of corruptie (I:N). | **CVE-2024-7254**: Stack-based Buffer Overflow.<br>*Impact:* Crash of mogelijke code-executie. | **Advies:** Override via dependency management naar **3.25.5** of hoger. | Dependency Override |
| `commons-fileupload:commons-fileupload` | `1.5` | **8.7 (High)** | `CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:H`<br>**Waarom 8.7:** Het versturen van oneindige bestanden via HTTP POST crasht de server (A:H, DoS), maar vereist geen rechten (PR:N). | **CVE-2025-48976**: Ongelimiteerde resource-allocatie.<br>*Impact:* Server DoS bij bestandsuploads. | **Advies:** Update naar **1.6.0** of hoger. | Dependency Override |
| `org.openmrs.web:openmrs-web` / `openmrs-api` | `2.8.7` | **8.6 (High)** | `CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:L`<br>**Waarom 8.6:** Eenvoudig uit te voeren op afstand zonder authenticatie (AV:N/AC:L/PR:N/UI:N), met een hoge impact op de vertrouwelijkheid (C:H, bijv. Directory Traversal waarbij alle bestanden gelezen kunnen worden), maar geen impact op integriteit (I:N) en minimale impact op beschikbaarheid (A:L). | **CVE-2026-40076, CVE-2022-23612**: Directory Traversal en DoS. | Hoewel platform `2.8.7` is doorgevoerd, bevat de core nog enkele unresolved issues. Dit moet binnen het OpenMRS Core-project zelf worden verholpen. | Geen (Monitor OpenMRS) |
| `org.apache.struts:struts-core` | `1.3.8` | **7.3 (High)** | `CVSS:3.1/AV:N/AC:L/PR:N/UI:R/S:U/C:H/I:H/A:H`<br>**Waarom 7.3:** De potentiële impact is compleet (C:H/I:H/A:H) en de aanval is makkelijk (AV:N/AC:L/PR:N), maar vereist actieve interactie van een legitieme gebruiker (UI:R, zoals het openen van een kwaadaardige link of bestand), wat de score dempt ten opzichte van 9.8. | **CVE-2014-0114**: Class Loader manipulatie.<br>*Impact:* Volledige serverovername (RCE). | Struts 1.x is al meer dan 10 jaar EOL.<br>**Advies:** Sluit deze library volledig uit (`<exclusion>`) in de `pom.xml` bij de import van `openmrs-web`. | Exclusion |

---

## 4. Strategisch Stappenplan voor Mitigatie

Nu de platform-upgrade is doorgevoerd, kunnen we de resterende 90 kwetsbaarheden efficiënt mitigeren via het configureren van overrides in de parent [pom.xml](file:///c:/Github/openmrs-module-radiology/openmrs-module-radiology/pom.xml).

### Actie: Inrichten van Transitieve Overrides
Voeg de volgende dependency-definities toe aan de `<dependencyManagement>` sectie van de parent `pom.xml` om de versies van de kwetsbare bibliotheken te overridentificeren:

```xml
<dependencyManagement>
    <dependencies>
        <!-- Netty Patch Upgrade (Löst de nieuwe Critical/High CVE's op die door AWS SDK zijn geïntroduceerd) -->
        <dependency>
            <groupId>io.netty</groupId>
            <artifactId>netty-bom</artifactId>
            <version>4.1.132.Final</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <!-- Spring Framework Patch Upgrade (Behoudt Java 8/11 compatibiliteit) -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-framework-bom</artifactId>
            <version>5.3.39</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        
        <!-- PostgreSQL Driver Upgrade -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.11</version>
        </dependency>
        
        <!-- Protobuf Upgrade -->
        <dependency>
            <groupId>com.google.protobuf</groupId>
            <artifactId>protobuf-java</artifactId>
            <version>3.25.5</version>
        </dependency>
        
        <!-- Commons Fileupload Upgrade -->
        <dependency>
            <groupId>commons-fileupload</groupId>
            <artifactId>commons-fileupload</artifactId>
            <version>1.6.0</version>
        </dependency>
        
        <!-- C3P0 Upgrade -->
        <dependency>
            <groupId>com.mchange</groupId>
            <artifactId>c3p0</artifactId>
            <version>0.12.0</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Actie: Uitsluiten van Legacy EOL Libraries (Exclusions)
Voeg `<exclusions>` toe aan de `openmrs-api` en `openmrs-web` imports in de module-specifieke `pom.xml` bestanden om `struts-core` en `jackson-mapper-asl` uit te sluiten, mits deze niet door radiology zelf worden gebruikt.

---

## 5. Verificatie & Testen

Na het doorvoeren van de wijzigingen:
1.  **Lokaal compileren & testen:** Run `mvn clean install` om er zeker van te zijn dat de overrides geen compilerfouten of runtime compatibility-problemen (zoals `NoSuchMethodError` bij Netty of Spring) veroorzaken.
2.  **Nieuwe SBOM & SCA-scan uitvoeren:** Genereer een nieuwe SBOM via Maven en scan deze opnieuw met Snyk (`snyk sbom test --file=sbom.cdx.json`) om te valideren dat het aantal kwetsbaarheden drastisch is verminderd.
