# Pipeline-compliance

In dit document wordt beschreven welke maatregelen in de GitHub-omgeving zijn ingericht en hoe deze bijdragen aan security, maintainability en de gekozen NEN-7510 controls.

| NEN-7510 control / onderwerp | Betekenis / risico | Pipeline-/GitHub-maatregel | Status | Bewijs |
|---|---|---|---|---|
| **A.8.3 / A.9.4.1** (Toegangsbeveiliging) | Toegang tot de repository en de `main`-branch moet beheerst worden. Zonder bescherming kunnen wijzigingen direct op `main` worden geplaatst. | Branch protection/ruleset voor `main`, waardoor wijzigingen via een Pull Request moeten verlopen en direct werken op `main` wordt beperkt. | Gedeeltelijk aanwezig | GitHub Rulesets-instelling voor `main`. Let op: GitHub geeft aan dat volledige enforcement op deze private repository afhankelijk is van het GitHub Team-plan. |
| **A.8.5 / A.9.4.2** (Authenticatie) | Gebruikers moeten veilig worden geauthenticeerd voordat zij toegang krijgen tot de repository en projectinstellingen. | MFA/2FA wordt gebruikt als beveiligingsmaatregel voor GitHub-accounts van teamleden. | Gedeeltelijk aanwezig | GitHub account-/organisatie-instellingen en teamafspraak dat MFA/2FA verplicht is voor repositorytoegang. |
| **A.8.15 / A.12.4.1** (Logging) | Security-relevante acties en pipeline-uitvoeringen moeten controleerbaar zijn. Zonder logging is het lastig om achteraf te controleren welke controles zijn uitgevoerd. | GitHub Actions bewaart logs van workflow-runs, waaronder de CodeQL-analyse. Hierdoor is zichtbaar wanneer de analyse is uitgevoerd en of deze succesvol was. | Aanwezig | GitHub Actions-run van `CodeQL Analysis` en bijbehorende workflow logs. |
| **A.8.28 & A.8.29 / A.14.2.5 & A.14.2.8** (Security & maintainability) | Kwetsbaarheden en onveilige codepatronen moeten vroeg in het ontwikkelproces worden gevonden. | CodeQL workflow toegevoegd voor statische codeanalyse van de Java/Kotlin-code. De workflow draait bij push, pull request en wekelijks via een schedule. | Aanwezig | [codeql-analysis.yml](file:///c:/Github/openmrs-module-radiology/.github/workflows/codeql-analysis.yml) en het SARIF-artifact `codeql-sarif-results` in GitHub Actions. |
| **A.8.8 & A.8.30 / A.12.6.1** (Supply-chain security) | Dependencies kunnen kwetsbaarheden bevatten. Deze moeten automatisch gecontroleerd worden. | Dependabot alerts staan aan en GitHub detecteert kwetsbaarheden in dependencies. | Aanwezig | GitHub Security/Dependabot alerts pagina. |
| **A.8.8 & A.8.30 / A.12.6.1** (Supply-chain maintainability) | Dependencies moeten periodiek gecontroleerd worden op updates, zodat verouderde componenten zichtbaar worden. | Dependabot-configuratie toegevoegd voor Maven en GitHub Actions. | Aanwezig | [dependabot.yml](file:///c:/Github/openmrs-module-radiology/.github/dependabot.yml). |
| **A.5.9 & A.5.19 / A.8.1.1 & A.15.1.1** (Software-inzicht / SBOM) | Gebruikte softwarecomponenten moeten inzichtelijk zijn voor security- en compliancecontrole. | SBOM-generatie wordt gebruikt om dependencies/softwarecomponenten inzichtelijk te maken. | Nog toe te voegen / gedeeltelijk aanwezig | `docs/sbom.cdx.json` of SBOM-artifact in GitHub Actions zodra deze taak is afgerond. |

---

## Gedetailleerde Toelichting per Maatregel

### 1. Branch Protection
* **NEN-7510 Controls**: A.8.3, A.8.25, A.8.32 / A.9.4.1, A.12.1.2, A.14.2.2
* **Werking**: Zorgt ervoor dat alle code door ten minste één ander teamlid is goedgekeurd via een Pull Request. Direct pushen naar de hoofdbranch is hiermee uitgeschakeld. Dit minimaliseert het risico dat ongeteste of onveilige code (zoals hardcoded credentials) in de hoofdcodebase belandt.

### 2. Multi-Factor Authentication (MFA / 2FA)
* **NEN-7510 Controls**: A.8.5, A.8.2 / A.9.4.2
* **Werking**: Het afdwingen van MFA voor alle teamleden met toegang tot de repository garandeert dat alleen geauthentiseerde ontwikkelaars wijzigingen kunnen indienen. Dit beschermt tegen de risico's van gelekte developer-wachtwoorden.

### 3. Dependabot (Vulnerability Scanning)
* **NEN-7510 Controls**: A.8.8, A.8.30 / A.12.6.1
* **Werking**: Dependabot controleert continu onze gebruikte bibliotheken tegen bekende kwetsbaarheidsdatabases. Bij kritieke problemen (zoals Zip Slip of Path Traversal in OpenMRS dependencies) waarschuwt het platform direct en genereert het automatisch een pull request met de benodigde patch.

### 4. CodeQL (Static Application Security Testing / SAST)
* **NEN-7510 Controls**: A.8.28, A.8.29 / A.14.2.5, A.14.2.8
* **Werking**: CodeQL voert een statische scan uit van de code op bugs, onveilige API-aanroepen en kwetsbaarheden (zoals XSS of het lekken van persoonsgegevens in logbestanden) vóórdat code mag worden gemerged.

### 5. SBOM (Software Bill of Materials)
* **NEN-7510 Controls**: A.5.9, A.5.19 / A.8.1.1, A.15.1.1
* **Werking**: Door in de pipeline een SBOM in CycloneDX-formaat te genereren, is er een sluitende administratie en inventaris van alle ingebouwde componenten. Dit bewijst supply chain compliance en maakt snelle audits mogelijk bij zero-day incidenten.