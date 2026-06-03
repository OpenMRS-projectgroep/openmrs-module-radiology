# Modulekeuze: openmrs-module-radiology

Dit document beschrijft de definitieve keuze en motivatie voor de `radiology`-module binnen OpenMRS als basis voor de security-audit.

## Algemene Gegevens
* **Modulenaam:** openmrs-module-radiology
* **Versie:** 0.1.2-SNAPSHOT
* **Upstream Repository:** [https://github.com/openmrs/openmrs-module-radiology](https://github.com/openmrs/openmrs-module-radiology)

---

### 1. Waarom deze module? (Kritieke functionaliteit)
We hebben gekozen voor de **radiology**-module omdat radiologie een heel belangrijk, maar ook risicovol onderdeel is binnen de zorgbeveiliging. De module voegt een Radiology Information System (RIS) toe aan OpenMRS.

* **Privacy en patiëntveiligheid:** Deze module koppelt patiëntgegevens direct aan uitslagen en medische beelden (zoals MRI- en CT-scans via DICOM). Als deze gegevens op straat komen te liggen of worden gegijzeld door hackers (ransomware), heeft dat direct grote gevolgen voor de behandelingen en de privacy van patiënten.
* **Architectonische risico's (Gat in de integratie):** Er zit een bekende beveiligings- en integratiebeperking in de architectuur. Radiologie-orders worden momenteel niet automatisch beveiligd naar het PACS (beeldenarchief) gestuurd, omdat OpenMRS een betrouwbare berichtenwachtrij (message queue) mist. In echte ziekenhuizen wordt dit nu opgelost met externe servers (zoals Mirth). Dit maakt het onderzoeken van de datastromen extra interessant voor een security-audit.
* **Harde richtlijnen voor productie:** Omdat het om medische gegevens gaat, verplicht de module het gebruik van een Reverse Proxy (zoals Nginx) met HTTPS (poort 443). Directe toegang via poort 8080 of 3306 (database) vanaf het internet is verboden. Dit geeft ons een concreet startpunt om de netwerkbeveiliging te testen.

### 2. Scope (Wat gaan we onderzoeken?)
De module is een compleet, full-stack systeem dat bestaat uit **301 bestanden** en bijna **48.000 regels code**. Dit is groot genoeg voor een serieuze audit, maar compact genoeg om het overzicht te bewaren. De scope is verdeeld over de volgende lagen:

* **De achterkant (Backend & Data):** De basislogica is geschreven in **Java JDK 8** (ruim 12.000 regels) met Maven. Ook kijken we naar de database-laag, die gebruikmaakt van SQL-bestanden (`demo-data.sql`) voor testdata.
* **De voorkant (Frontend):** De interface gebruikt nu nog de OpenMRS Legacy UI. Er loopt momenteel een transitie (ticket RAD-341) om de UI los te koppelen zodat de module puur als REST API gaat werken. We onderzoeken de huidige interface (JavaScript en JSP, samen zo's 10.500 regels) op zwakke plekken.
* **De installatie & Omgeving (DevOps):** De module ondersteunt een Docker-architectuur (`docker-compose.test.yml`). De scope bevat daarom ook het controleren van de container-beveiliging en de configuratie van gevoelige omgevingsvariabelen (zoals de `MYSQL_ROOT_PASSWORD` in het `.env` voorbeeld).

### 3. Complexiteit (Niet te makkelijk, niet te moeilijk)
De complexiteit van deze module is precies goed voor onze opdracht:

* **Niet te makkelijk (Voldoende uitdaging):** Met een totale complexiteitsscore van **2.049** zitten er flink wat logische stappen en keuzes (`if/else`-situaties) in de code. Er is dus genoeg diepgang om te zoeken naar fouten in de logica of zwakke plekken in de code.
* **Interessante front-end:** De meeste ingewikkelde code (**1.462**) zit in de JavaScript-bestanden aan de voorkant. Dit maakt het heel interessant om te testen of de invoervelden en schermen goed beveiligd zijn tegen bijvoorbeeld hackpogingen via de browser (zoals Cross-Site Scripting).
* **Overzichtelijke back-end:** De Java-code aan de achterkant heeft een lagere complexiteitsscore van **471**. De backend is dus relatief netjes en modulair opgebouwd. Hierdoor kunnen we heel gericht en overzichtelijk controleren of de toegangsrechten (*Access Control*) goed zijn geprogrammeerd.

> **Conclusie:** De `radiology`-module (v0.1.2-SNAPSHOT) is qua complexiteit de ideale 'gouden middenweg'. Door de verwerking van privacygevoelige medische beelden, de Docker-omgeving en de strenge netwerkvorschriften (Reverse Proxy/HTTPS) biedt het alle ingrediënten voor een realistische en leerzame security-audit.