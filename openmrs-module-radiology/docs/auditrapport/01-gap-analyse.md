# NEN-7510 Gap-analyse: OpenMRS Radiology Module

Dit document beschrijft de gap-analyse van de **OpenMRS Radiology Module** ten opzichte van de **NEN-7510:2024-2** norm. Hierin evalueren we in hoeverre de module voldoet aan de gestelde controls en leveren we bewijslast uit de code.

---

## 📑 Overzicht van Controls

| Control ID | NEN-7510:2024 Control | Status | Samenvatting Bewijslast |
| :--- | :--- | :---: | :--- |
| **A.8.3** | **Toegangsbeveiliging** | **Aanwezig** | Privileges gedefinieerd via Liquibase, afgedwongen met `@Authorized` AOP-annotaties op de service-laag en `<openmrs:hasPrivilege>` tags in JSP-bestanden. |
| **A.8.5** | **Authenticatie** | *Nog te onderzoeken* | *Wordt ingevuld in volgende taak* |
| **A.8.15** | **Logging** | *Nog te onderzoeken* | *Wordt ingevuld in volgende taak* |

---

## 🔒 Control A.8.3: Toegangsbeveiliging (Access Control)

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

## 🔑 Control A.8.5: Authenticatie (Authentication)

*Nog uit te voeren gap-analyse.*

---

## 📝 Control A.8.15: Logging (Activity Logging)

*Nog uit te voeren gap-analyse.*
