## Version

0.1.2-SNAPSHOT

## Upstream Repository Link

https://github.com/openmrs/openmrs-module-radiology

## Architecture & Responsibilities

De module is ontworpen om een Radiology Information System (RIS) toe te voegen aan het OpenMRS-platform.
De architectuur kenmerkt zich door de volgende aspecten:

Core & Tech stack: Gebouwd op Java JDK 8 met Maven als build-tool.

Ontkoppeling van de UI (Gepland/Lopend): De module is momenteel nog afhankelijk van de OpenMRS Legacy UI om de interface te leveren.
Er is echter een actieve transitie (geregistreerd onder ticket RAD-341) om de UI volledig te scheiden, zodat deze module puur als een Java en REST API fungeert.

Integratiebeperking (PACS/HL7): Er is een kritieke architectonische beperking gemarkeerd: radiologie-orders worden momenteel niet automatisch als HL7-berichten
naar het PACS (Picture Archiving and Communication System) verzonden.
OpenMRS mist een uitgaande berichtenwachtrij (message queue) om dit betrouwbaar (met retry-mechanismen) af te handelen.
In productieomgevingen wordt dit gat momenteel overbrugd met externe communicatieservers zoals Mirth.

Containerisatie: De module ondersteunt een Docker-architectuur, waardoor de applicatie (inclusief OpenMRS core) gemakkelijk als een container kan worden gebouwd en gedraaid.

Data Layer: Ondersteunt het importeren van demodata via SQL (demo-data.sql) voor test- en ontwikkeldoeleinden.

## Deployment & Security (Productie-richtlijnen)

Voor een veilige test- of productieomgeving moeten de netwerkpoorten strikt worden afgeschermd conform het principe van 'least privilege'.

### Netwerk- & Poortbeperkingen

1. **Database-isolatie (Poort 3306):**
    * De database-container mag **nooit** een publieke poort-mapping naar de host-machine hebben.
    * Zorg ervoor dat de database uitsluitend communiceert via een geïsoleerd, intern Docker-netwerk dat alleen toegankelijk is voor de OpenMRS-applicatiecontainer.

2. **Applicatie-afscherming (Poort 8080):**
    * Stel de applicatie-container zo in dat de HTTP-poort (standaard 8080) uitsluitend luistert op de lokale loopback-interface (`127.0.0.1`).
    * **Directe toegang vanaf het internet via HTTP is niet toegestaan.**

### Reverse Proxy & HTTPS
Om medische patiëntgegevens (RIS/PACS) te beschermen, is het verplicht om een Reverse Proxy (zoals Nginx, Apache of Caddy) voor de OpenMRS-container te plaatsen.

* De Reverse Proxy dient extern poort `443` (HTTPS) open te stellen en het SSL/TLS-certificaat te beheren.
* Intern stuurt de proxy het verkeer veilig door naar de lokale loopback-poort van de OpenMRS-container.

## Testomgeving (Integration & Acceptance Testing)

Voor het draaien van geautomatiseerde of handmatige acceptatietests is een aparte testconfiguratie beschikbaar die automatisch wordt geladen met demodata.

### Testomgeving opstarten
Om de geïsoleerde testomgeving inclusief de `demo-data.sql` dataset te starten, voer uit:
mvn test -pl api
mvn test -pl api -Dtest=*Dicom*

```bash
docker compose -f docker-compose.test.yml up -d
```

## .env example
MYSQL_ROOT_PASSWORD: Minimaal 16 willekeurige tekens.
MYSQL_PASSWORD: Een uniek wachtwoord voor de openmrs database-user.