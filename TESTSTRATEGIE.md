# Test-Strategie

> Dieses Dokument enthält Teil 1 (Abschnitte 1–9, 35 % der Bewertung) und Teil 3
> (Abschnitt 10, 15 % der Bewertung) der Challenge. Fülle die Abschnitte aus.
> Entferne diese Hinweiszeile und die kursiven Hinweise unter den Überschriften, bevor du abgibst.
> Es gibt keine Mindestlänge – Begründung zählt mehr als Umfang. Abschnitte, die du für
> nicht anwendbar hältst, darfst du streichen, wenn du die Entscheidung kurz begründest.

**Bearbeitet von:Friedl Thomas**
**Datum:07.10.2026**

---

## 1. Scope und Zielsetzung
*
Tests für die 7 Kundenanforderungen, API, UX, Manuelles Testen

Test der Kundenanforderungen. Test für eine erfolgreiche Abnahme und Dokumentation der Punkte im Lastenheft bei Abnahme und Übergabe an den Kunden.
API Tests um die Stabilität der Anwendung zu Testen
UX-Tests um die Oberfläche zu Testen
Manuelle Tests um aus Sicht eines Benutzers und die Bedienbarkeit Testen zu können.

Die Tests können jeweils mit den Kommondos gestartet werden
frontent: npm run test:e2e
backend: .\mvnw.cmd test 

*
---

## 2. Risikoanalyse und Risikobasierung

*
Risikoanalyse: 
Je nachdem ob das System als Service angeboten wird oder OnPremise beim Kunden läuft.

Inhous/Service, muss die Stabilität der Datenschnittstellten und Durchsatzrate/Stresstest sichergestellt sein! Das Service muss 24/7 zur Verfügung stehen schon kleine % Werte an Ausfällen kann hohe Kosten verursachen. Wartungsarbeiten in Abstimmung mit den Kunden Kommunizieren.

On Premise beim Kunden, muss die Stabilität des gesamten Produktes sichergestellt sein. Die API wird wohl weniger belastet sein, muss jedoch auch verlässlich funktionieren. UX und Anwendertest liegen hier zusätlich im Fordergrund. Bei jedem high Prio Punkt, muss potentiell eine neue Version/ Update dem User zur Verfügung gestsellt werden.

Generell zur Risikoanalyse und daraus ergebende Defect Kategorie.
Ist im klassischen Fall mit Auswirkung und Eintrittswahrscheinlichkeit zu bewerten. Jeweils High, Medium, Low
Erweitert kann man noch einen Faktor für das Implementierungsrisiko/Änderungsrisiko hinzufügen.
Es ergeben sich daraus die Defect-Kategorien A bis D:

- **A:** Alles andere zurückstellen und den Defect sofort beheben, weil die Anwendung abstürzt oder eine Funktion falsch beziehungsweise gar nicht funktioniert.
- **B:** Die Funktion der Anwendung kann durch eine Umgehung gewährleistet werden, zum Beispiel durch das vorübergehende Deaktivieren eines speziellen Features.
- **C:** Die Funktionsweise ist nicht wesentlich beeinträchtigt, der Fehler ist für den User jedoch unangenehm oder auffällig.
- **D:** Ein kleiner Schönheitsfehler, mit dem bis zu einer späteren Major-Version gelebt werden kann.
*
---

## 3. Teststufen und Verteilung des Aufwands

*Testpyramide: 
Unit Test -> bilden die unterste Ebene der Pyramide -> leichtgewichtig schnell, hohe Anzahl
Integration Tests -> Mittlere Schicht. Nach einem Sprint, werden fertige Teile eines neuen Features gemeinsam mit dem schon vorhandenen Produktiven Code getestet. um den Blick zu weiten und Links Rechts zu testen ob der Neue Code weitgehend mit älteren Komponenten funktioniert.
E2E Test -> sehr aufwendig, Kosten und Ressourcen Intensive, Werden benötigt um ein Gesamtbild der Neuen Features in Zusammenarbeit mit der bestehenden Softwarelöstung zu erhalten. Gerne auch manuelle Tests. Komplettes Testsetting, Protoype beim Testen.

Vollständige API Test nicht E2E, Nach einem Automatisierten Testablauf kann ein TestLog der einzelnen Tests noch manuell geprüft werden.*

---

## 4. Testarten

*Usability: Manuelle Tests um das UX und die Verwendung der Benutzeroberlfäche abzutesten. Kundennaher Test
Performance: um die Performance und Reaktionszeiten der API abzutesten, während der Entwicklung um hode Laufzeitänderungen festzustellen. Nach jedem Release, wie sich die Performance über die zeit entwickelt. Bei konstanter Hardware. Um zu Entscheiden wann, ab welcher Version eine neue HW eingesetzt werden muss.
Grenzwerte: z.b. bei der Begründung "nicht mehr als 255 Zeichen", Datum eingabe, 
Zustandsübergabe: Nein, Kein Zustand! LogIn, LogOut? kein Schrittweises vorgehen
Nebenläufigkeit: Racecondition, als Service, wenn mehrere Patienten gleichzeitig den gleichen Slot buchen wollen.
Security, kein Log In kein Log Out? API Sicherheit?*


---

## 5. Wahl des Test-Stacks

*Automatisierte Tests. Backend, Spring API Test Framework MockMvc, bietet sich an, weil die API auch auf Spring aufbaut. Performant und leicht integrierbar. Extra Unit Tests macht vl für einzelne Erweiterungen der Anwendung sinn. Komplexe berechnungen. Soll/ist vergleich 

Frontend tests in Playright, Web basiert, UX Tests, Typescript, 
Alternativen: Slenium, benötigt mehr Konfigurartion, Vitest, für Formulare und Komponententests

Manuelle Tests ergänzen automatisierte Tests bei Integrationstests und E2E Tests, oder Gesamtsystemtests*


---

## 6. Testdaten und Testumgebung

*Macht für kleinere Tests sinn, da die Tests und Daten im Arbeitsspeicher liegen. Eine Db liefert auch die Testumgebung out of the box mit. Liefert reine Funktions Tests. Leider nur wenig Aussage über Performance, muss mit einem Echtsystem und Echthardware zusätlich getestet werden.

Bei umfangreicheren Tests können Die Datan jeweils aud der DB gelöscht werden um einen Urzustand zu erhalten. -> wird hier nicht gemacht.

Große Datenmengen, z.b. bei komplexen Berechnungen macht es Sinn eine Tabelle mit Sollwerten zu hinterlegen. Damit wird sichergestellt, dass kirtische berechnungne nicht falsch berechnet werden. Ist/Soll Abgleich -> Zeitabhängigkeit, Taskreihenfolge.

DB Tests für alle Lese und Schreibeoperationen, auch verschachtelt und Parallel.*

---

## 7. Automatisierungsgrad und CI-Einbindung

*Was automatisierst du, was bleibt manuell? Wie sähe die Einbindung in eine Pipeline aus –
welche Tests laufen bei jedem Commit, welche nächtlich, welche vor einem Release?*

CI Einbindung. 
Eine Basis Testabdeckung um die Stabilität der Kernkomponenten zu gewählreisten, läuft bei jedem Commit. Eher Backend und API lastig.
Zusätlich laufen über Nacht größere und mehr Unit tests automatisiert ab. Automatisierte Integrationstests über mehrere Ausbaustufen und Projekte, die den selben, neuen Code verwenden.
Nach Sprint, gibt es die ersten Manuellen integrationstest, ein Basistest für eine Fertige Sprint Version.
UX Tests müssen auch Manuell durchgeführt werden. Um eitweige Sideeffekts in einer bestimten Maske, Oberfläche zu finden. Der großteil der Manuellen Tests findet aber erst Am Ende der Implementierung, bzw. vor einem Release statt. Hier werden nochmal alle neuen Features im Zusammenspiel mit der Gesamtanwenung aus Sicht eines Anwenders abgetestet. 

---

## 8. Exit-Kriterien und Metriken

*Woran machst du fest, dass ausreichend getestet wurde? Welche Metriken erhebst du, und
welche Aussagekraft haben sie tatsächlich?*

Testabdeckung/Testcoverage: wie weit wird der Gesamt Code durch tests abgetestet, Wie weit wird der Neue Code abgetestet. Anderung von Release zu Release.
Anzahl der Defect, Defect Dichte: Defects bezogen auf die göße des Projekts. Kritische Defects im Projekt
Fehlererkennung: Wieviele Defects werden mit den Tests gefunden. Was wird übersehen? und Tritt erst bei späteren Tests auf. Je näher an der Implemntierung desto besser.
Neu entstehende Probleme bei erweiterung der Anwendung: Anwenungslebenszyklus, Erweiterbarkeit
Defect Behebungszeit: kann eine Behebung zeitnah durchgeführt werden. Änderungsrisiko fließt auch in die Defectbewertung ein

---

## 9. Gefundene Defects

*Pro Defect: kurze Beschreibung, Reproduktionsschritte, erwartetes und tatsächliches
Verhalten, betroffene Anforderung aus der README, Schweregrad mit Begründung und ein
Verweis auf den Test, der ihn nachweist.*

| # | Titel | Anforderung | Schweregrad | Nachweis (Playwright) | Backend-Nachweis |
|---|---|---|---|---|---|
| 1 | Parallele Buchungen desselben Slots werden beide angenommen | 1: genau eine Buchung je Slot; zweite Buchung muss `409` liefern | Hoch – führt bei zeitgleichen Anfragen zu Doppelbuchungen | [`laesst auch bei gleichzeitigen Buchungen nur eine Buchung zu`](./frontend/tests/anforderungen.spec.ts) | [`laesstAuchBeiGleichzeitigenBuchungenNurEineBuchungZu`](./backend/src/test/java/at/cgm/termine/web/TerminAnforderungenTest.java) – derselbe Fehler tritt über die API auf |
| 2 | Unbekannte Slot-ID verursacht internen Fehler | 6: unbekannte Slot-ID muss `404` liefern | Hoch – ungültiger Client-Aufruf endet als Serverfehler `500` statt als definierter fachlicher Fehler | [`liefert fuer eine unbekannte Slot-ID 404`](./frontend/tests/anforderungen.spec.ts) | [`unbekannteSlotIdErgibt404`](./backend/src/test/java/at/cgm/termine/web/TerminAnforderungenTest.java) – API liefert ebenfalls nicht `404` |
| 3 | Bereits stornierter Termin kann erneut storniert werden | 5: zweite Stornierung muss `409` liefern | Mittel – erlaubt eine ungültige Zustandsaktion und erzeugt irreführendes Feedback | [`lehnt eine zweite Stornierung desselben Termins mit 409 ab`](./frontend/tests/anforderungen.spec.ts) | [`bereitsStorniertenTerminKannManNichtErneutStornieren`](./backend/src/test/java/at/cgm/termine/web/TerminAnforderungenTest.java) – derselbe Fehler tritt über die API auf |
| 4 | Stornierung weniger als 24 Stunden vor Beginn wird akzeptiert | 3: nach Ablauf der 24-Stunden-Frist muss `409` geliefert werden | Hoch – Termin wird entgegen der Geschäftsregel freigegeben | [`lehnt die Stornierung weniger als 24 Stunden vor Beginn mit 409 ab`](./frontend/tests/anforderungen.spec.ts) | [`storniertTerminWenigerAls24StundenVorBeginnNicht`](./backend/src/test/java/at/cgm/termine/web/TerminAnforderungenTest.java) – API akzeptiert die Stornierung ebenfalls |
| 5 | Besuchsgrund mit mehr als 255 Zeichen wird akzeptiert | 7: Grund höchstens 255 Zeichen; Verletzung muss `400` liefern | Mittel – ungültige Daten gelangen in den Terminbestand und können Anzeige oder Verarbeitung beeinträchtigen | [`lehnt einen Besuchsgrund mit mehr als 255 Zeichen mit 400 ab`](./frontend/tests/anforderungen.spec.ts) | [`lehntBesuchsgrundMitMehrAls255ZeichenMit400Ab`](./backend/src/test/java/at/cgm/termine/web/TerminAnforderungenTest.java) – API akzeptiert den Grund ebenfalls |
| 6 | Arzt und Beginn werden in „Meine Termine“ nicht angezeigt | UI-Darstellung der Terminangaben; fachliche Termindaten sollen für Nutzer erkennbar sein | Hoch – Nutzer können Arzt und Terminzeit des gebuchten Termins nicht in der Terminübersicht erkennen | [`zeigt Arzt und Beginn eines gebuchten Termins unter Meine Termine an`](./frontend/tests/ux.spec.ts) | Kein gleichwertiger Backend-Test: Fehler betrifft die Darstellung in der Oberfläche |
| 7 | Lange Patienten- und Grundtexte verbreitern die Tabelle massiv | Nutzbare Darstellung der Buchungsdaten in der Terminübersicht | Mittel – Tabelle wächst bei zulässigen Textlängen auf ca. 3.970 px bei 858 px Inhaltsbreite; Spalten und weitere Inhalte werden schwer nutzbar | [`begrenzt die Tabellenspalten bei langen Patienten- und Besuchsgrundtexten`](./frontend/tests/ux.spec.ts) | Kein gleichwertiger Backend-Test: Tabellenlayout und Spaltenbreite sind UI-Verhalten |
| 8 | Stornierter Termin behält die Aktion „Stornieren“ | UI soll den aktualisierten Zustand nach erfolgreicher Stornierung korrekt darstellen | Mittel – Aktion bleibt sichtbar und verleitet zu einer Wiederholung, die fachlich nicht zulässig ist | [`zeigt nach dem Stornieren die Erfolgsmeldung und den Slot wieder als frei`](./frontend/tests/ux.spec.ts) | [`bereitsStorniertenTerminKannManNichtErneutStornieren`](./backend/src/test/java/at/cgm/termine/web/TerminAnforderungenTest.java) – fachlich verwandt: prüft erneute Stornierung per API, aber nicht die sichtbare Schaltfläche |

### Reproduktionsdetails

1. **Parallele Doppelbuchung:** Per API einen freien Slot anlegen; zwei Buchungsanfragen für denselben Slot gleichzeitig absenden. Erwartet: genau eine Antwort `201`, die andere `409`. Tatsächlich: beide Anfragen liefern `201`.
2. **Unbekannte Slot-ID:** `POST /api/appointments` mit einer nicht vorhandenen `slotId` und ansonsten gültigen Feldern senden. Erwartet: `404` im Fehlerformat `{"fehler":"..."}`. Tatsächlich: `500` (NullPointerException im Backend).
3. **Erneute Stornierung:** Einen Slot mehr als 24 Stunden in der Zukunft anlegen, buchen und denselben Termin zweimal über `DELETE /api/appointments/{id}` stornieren. Erwartet: erster Aufruf erfolgreich, zweiter `409`. Tatsächlich: auch der zweite Aufruf liefert `200`.
4. **Stornierungsfrist:** Einen Slot weniger als 24 Stunden in der Zukunft anlegen, buchen und stornieren. Erwartet: `409`. Tatsächlich: Stornierung wird mit `200` akzeptiert.
5. **Zu langer Besuchsgrund:** Einen zukünftigen Slot anlegen und mit einem 256 Zeichen langen Grund buchen. Erwartet: `400`. Tatsächlich: Buchung wird mit `201` akzeptiert.
6. **Fehlende Termindetails:** Über die API einen Termin buchen und die Seite öffnen. Erwartet: Arzt und Beginn stehen in der Zeile unter „Meine Termine“. Tatsächlich: beide Tabellenzellen sind leer.
7. **Tabellenüberlauf:** Einen Termin mit 100 Zeichen langem Patientennamen und 255 Zeichen langem Grund anlegen und die Terminübersicht bei 1024 px Viewport betrachten. Erwartet: Tabelle bleibt innerhalb des Abschnitts. Tatsächlich: Tabelle ist ca. 3.970 px breit, obwohl der Abschnitt 858 px breit ist.
8. **Storno-Aktion bleibt sichtbar:** Über die API einen zukünftigen Termin anlegen, in der Oberfläche stornieren und die Zeile erneut betrachten. Erwartet: Erfolgsmeldung, Slot wird wieder frei und für den stornierten Termin wird keine Storno-Aktion mehr angeboten. Tatsächlich: die Erfolgsmeldung erscheint und der Slot wird frei, aber der Button „Stornieren“ bleibt am stornierten Termin sichtbar.

---

## 10. KI in der QA

*Der Einsatz von KI-Werkzeugen bei dieser Aufgabe ist ausdrücklich erlaubt und erwünscht.
Es gibt keine Punkte dafür, auf KI zu verzichten, und keine Abzüge für ihren Einsatz.
Bewertet wird allein, wie differenziert du über Nutzen, Grenzen und Risiken urteilst.*

### 10.1 Einsatzpotenzial in diesem Projekt

*An welchen konkreten Stellen der QA dieses Projekts würdest du KI einsetzen – und wie
genau? Nenne mindestens zwei Stellen, beschreibe den konkreten Einsatz (nicht „KI für
Testfallgenerierung", sondern wofür genau, mit welchem Input und welchem Ergebnis) und
begründe, warum sich der Aufwand dort lohnt. Wo würdest du bewusst darauf verzichten?*

1> zur Analyse des Programmumfangs. Was kann, bzw soll das Programm können.
2> Testfall erstellung für die 7 Kundenanforderungen jeweils Backend und Frontend, Input die Kundenanforderungen


### 10.2 Risiken

*Welche Risiken siehst du beim KI-Einsatz in der QA – fachlich, methodisch und
organisatorisch? Wie würdest du ihnen begegnen? Denk auch an den Fall, dass KI die
Spezifikation selbst interpretiert.

Die KI ist ein Sprachmodell damit kann es gut bei Testerstellung, Codeerstellung, Formatierung, Dokumentation unterstützen und helfen. 
Logische Schritte, warum etwas so ist wie es ist, kann logisch aus einem Codeteil interpretiert werden. Aber daraus resultierende Schlussfolgerungen können nicht sicher erstellt werden. Gefahr für den Anwender. Man benötigt für die sichere Anwendung immer einen Menschen, der die Anforderung kennt und die daraus resultierende Programmierung verstehen kann. Warum ist das so umgesetzt.

Generelle Gedanken:
Es verleitet einem Recht zu geben und eine Lösung nicht mehr zu hinterfragen. Ein einfacher schneller Weg! Ein einfacher schneller falser Weg?
Die KI will einem gefallen! Daher gibt es immer eine Andwort, auch wenn diese vermeindlich Falsch ist, wird sie als Richtig angzeigt.
Wie Validiere ich die Aussagen, Fähigkeiten einer KI. Traue ich der KI mehr als einen Menschen? Macht eine Maschine Fehler?
Im großen und Ganzen muss man heraus finden, wo man ki gezielt einsetzen kann und einen Mehrwert bieten kann. Als sparings Partner.
Wo kannm bzw darf man KI nicht einsetzen? Ist Dr Google ein echter Artzt?
*

### 10.3 Eigener Einsatz bei dieser Aufgabe

*Wo hast du bei der Bearbeitung KI eingesetzt, mit welchem Werkzeug, zu welchem Zweck?
Was hat gut funktioniert, was nicht? Gab es ein Ergebnis, das du verworfen oder korrigiert
hast – und woran hast du den Fehler bemerkt? Falls du keine KI eingesetzt hast, begründe
diese Entscheidung.*
1> punkt 10.1, bietet einen ersten Einstig und Übersicht
2> punkt 10.1, damit kann man auch ohne den gesamten Umfang einer Anwendung zu kennen erste Testfälle erstellen.
3> UX Tests, bei eingabe des Fehlerbildes. zb. unter Meine Termine wird bei Artzt und Beginn nichts angezeigt. Erstelle einen Testfall!
4> Erstellung der Tabelle in Punkt 9. Ich habe den Schweregrad ergänzt, bzw angepasst

---

## 11. Was ich mit mehr Zeit zusätzlich getan hätte

-> ich hätte gerne die Testfälle mit einem Jira Workitem im Verbindung gebracht. Für jeden TestCase sollte es auch ein Jira Workitem mit einer Nummer geben. Diese in den Testfallnamen einbringen. Testpakte in Jira bilden Testklassen im Testframework ab

-> Dieser Code ist in Deutsch geschrieben! Warum?

-> gezielte Dokumentation der Klassen, wofür diese gedacht ist und was sie leisten soll

-> mir ist bewusst, dass ich hier nicht alles und Umfragreich getestet habe. Mehr Zeit, wie verhät sich ein User? Wie funktioniert die Anwendung in einem Realen Szenario? Ist das ein Reales Kundenprojekt?