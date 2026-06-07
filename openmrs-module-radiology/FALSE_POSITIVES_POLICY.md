# Beleid en Procesafspraak: Omgaan met False Positives (SAST & SCA)

Dit document beschrijft de officiële procesafspraken binnen het ontwikkelteam voor het identificeren, beoordelen, documenteren en negeren van onterechte beveiligingsmeldingen (*False Positives*) die worden gegenereerd door onze security-scanners (**CodeQL** voor SAST en **Dependabot** voor SCA).

---

## 1. Doelstelling
Geautomatiseerde security-tools zijn essentieel voor het bewaken van onze softwarekwaliteit, maar ze genereren inherent *False Positives* (valse alarmen). Het doel van dit beleid is om te voorkomen dat:
1. Kwetsbaarheden onterecht worden genegeerd (met beveiligingsrisico's tot gevolg).
2. Het team 'alert-moeheid' ontwikkelt door een overvloed aan irrelevante meldingen.
3. Er een gebrek aan traceerbaarheid ontstaat over *waarom* bepaalde meldingen zijn gesloten.

---

## 2. Wat is een legitieme False Positive?

Een melding mag **alleen** als False Positive of acceptabel risico worden gemarkeerd als deze aan minimaal één van de volgende criteria voldoet:

### A. Voor SAST (CodeQL - Eigen code)
* **Onjuiste context:** CodeQL herkent een patroon als kwetsbaar (bijv. mogelijke SQL-injectie), maar in de praktijk is de invoer al sluitend gesaneerd of gevalideerd door een framework-functie die CodeQL niet herkent.
* **Testcode:** De melding bevindt zich in testbestanden (`/tests`, `*.test.js`, etc.) en heeft geen enkele impact op de productie-omgeving.

### B. Voor SCA (Dependabot - Externe bibliotheken)
* **Functie niet in gebruik:** Een open-source bibliotheek bevat een kwetsbaarheid, maar onze applicatie importeert of gebruikt de specifieke kwetsbare module/functie van die bibliotheek aantoonbaar **niet**.
* **Geen misbruikpad:** De kwetsbaarheid vereist een specifieke configuratie of omgevingsfactor die binnen onze architectuur technisch onmogelijk is.

---

## 3. Het Proces: Stappenplan bij een melding

Wanneer een ontwikkelaar een security-alert tegenkomt in de pipeline of op het GitHub Dashboard, wordt het volgende stappenplan verplicht doorlopen:

1. **Onderzoek & Verificatie:** De ontwikkelaar onderzoekt de melding. Er wordt gekeken naar de exacte locatie, de dataflow (bij SAST) of de release notes en CVE-details (bij SCA). Is het een echt lek? Dan wordt er direct een patch of update ingepland.
2. **Het Vier-Ogen Principe (Peer-review):** Een ontwikkelaar mag **nooit zelfstandig** een melding als False Positive markeren en sluiten. Het vermoeden moet altijd worden voorgelegd aan een tweede ontwikkelaar (of de Security Champion van de groep). Pas bij akkoord van beiden gaan we naar stap 3.
3. **Technisch markeren:** De melding wordt in de tool of code genegeerd (zie paragraaf 4).
4. **Loggen met argumentatie:** De reden van sluiten wordt sluitend gedocumenteerd (zie paragraaf 5).

---

## 4. Instructies voor het markeren en negeren

Afhankelijk van de tool gelden de volgende technische richtlijnen voor het negeren van de melding:

### 4.1 Dependabot (SCA)
Valse Dependabot-meldingen worden afgehandeld via de GitHub-interface:
1. Navigeer naar de tab **Security** -> **Dependabot alerts**.
2. Klik op de betreffende alert om de details te openen.
3. Klik rechtsboven op de knop **Dismiss alert**.
4. Selecteer de meest accurate reden (bijv. `Vulnerable code is not used` of `Inaccurate alert`).
5. **Verplicht:** Voeg in het tekstveld een heldere motivatie toe.

### 4.2 CodeQL (SAST)
Voor CodeQL heeft het de voorkeur om de uitzondering **in de code zelf** te documenteren middels een comment. Hierdoor blijft de context bewaard als code wordt verplaatst.

* Gebruik een specifieke CodeQL-waarschuwingsannotatie direct boven de regel waar de fout optreedt.
* *Voorbeeld (JavaScript/TypeScript):*
  ```javascript
  // codeql[js/sql-injection] False positive: Input is strikt gevalideerd via regex in de API Gateway, misbruik is onmogelijk.
  const query = "SELECT * FROM users WHERE id = " + userInput;