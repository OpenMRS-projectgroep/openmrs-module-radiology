### 1. Waarom deze module? (Kritieke functionaliteit)
We hebben gekozen voor de **radiology**-module omdat radiologie een heel belangrijk en gevoelig onderdeel is binnen de zorg.

* **Privacy en veiligheid:** Deze module koppelt patiëntgegevens direct aan uitslagen en medische beelden (zoals MRI- en CT-scans). Als deze gegevens op straat komen te liggen of worden gegijzeld door hackers (ransomware), heeft dat direct grote gevolgen voor de patiënt en de behandelingen.
* **Goed voorbeeld:** Omdat er zoveel privacygevoelige medische data in omloop is, is dit de perfecte module om te controleren of de beveiliging (zoals het inloggen en de toegangsrechten) goed is geregeld.

### 2. Scope (Wat gaan we onderzoeken?)
De module is een compleet systeem dat bestaat uit **301 bestanden** en bijna **48.000 regels code**. Het is groot genoeg om een serieuze test op te doen, maar niet zó groot dat we erin verdwalen. We kijken naar de volgende onderdelen:

* **De achterkant (Backend):** De basislogica die is geschreven in **Java** (ruim 12.000 regels). Hierin worden de belangrijkste beslissingen en de databasekoppelingen geregeld.
* **De voorkant (Frontend):** Het scherm dat de zorgverlener ziet, gemaakt met **JavaScript** en **JSP** (samen zo'n 10.500 regels).
* **De installatie (DevOps):** Er zitten ook installatiebestanden bij (zoals een Dockerfile), waardoor we kunnen kijken of het installeren van de module veilig gebeurt.

### 3. Complexiteit (Niet te makkelijk, niet te moeilijk)
De complexiteit van deze module is precies goed voor onze opdracht:

* **Niet te makkelijk:** Met een totale complexiteitsscore van **2.049** zitten er flink wat logische stappen en keuzes (`if/else`-situaties) in de code. Dit betekent dat er genoeg diepgang in zit om echt op zoek te gaan naar fouten of zwakke plekken in de beveiliging.
* **Interessante front-end:** Opvallend is dat de meeste ingewikkelde code (**1.462**) in de JavaScript-bestanden (de voorkant) zit. Dit maakt het extra interessant om te kijken of de website zelf goed beveiligd is tegen bijvoorbeeld hackpogingen via de browser.
* **Overzichtelijke back-end:** De Java-code aan de achterkant heeft een score van **471**. Dit is relatief overzichtelijk en netjes opgebouwd, waardoor we heel gericht kunnen controleren of de controle op toegangsrechten goed is geprogrammeerd.

> **Conclusie:** De `radiology`-module is niet te simpel en niet te ingewikkeld. Omdat het met gevoelige medische gegevens werkt, is het voor ons de ideale module om een goede en realistische beveiligingstest op uit te voeren.