# Implementierungsplan - Dedalus Aufgabe (Spring Boot + Angular 21)

## 1. Ziel und Rahmen
- Es wird eine Webapplikation gebaut, die fuer einen Euro-Betrag die Stueckelung mit moeglichst wenigen Scheinen/Muenzen berechnet (Greedy: groesste Einheit zuerst).
- Ab der zweiten Berechnung wird die Differenz zur vorherigen Stueckelung angezeigt.
- Die Berechnung ist in der UI umschaltbar:
- Frontend-Modus: Berechnung in Angular.
- Backend-Modus: Berechnung ueber Spring Boot REST API.
- UI-Komponenten werden nach Moeglichkeit aus PrimeNG genutzt, keine eigenen komplexen UI-Widgets.

## 2. Fachliche Regeln aus der Aufgabe
- Verfuegbare Einheiten in EUR:
- `200.00`, `100.00`, `50.00`, `20.00`, `10.00`, `5.00`
- `2.00`, `1.00`, `0.50`, `0.20`, `0.10`, `0.05`, `0.02`, `0.01`
- Berechnung erfolgt exakt auf Cent-Basis (Integer-Cent), um Floating-Point-Fehler zu vermeiden.
- Differenzanzeige: pro Einheit `delta = neueAnzahl - alteAnzahl`.
- Die letzte Berechnung wird im aktiven Modus als `previous` fuer die naechste Berechnung verwendet.

## 3. Architekturziel
- Backend:
- Maven/Spring-Boot-Projekt im Root bleibt Serverteil.
- Backend speichert die letzte Berechnung serverseitig (zunaechst In-Memory, spaeter optional persistent).
- REST API stellt Berechnung und Synchronisation der letzten Berechnung bereit.
- Frontend:
- Eigenes Angular-21-Projekt in `frontend/`.
- PrimeNG fuer Formulare, Tabellen und Layout.
- Kommunikation:
- Lokaler Dev-Betrieb mit zwei Prozessen (`Spring Boot :8080`, `Angular :4200`), CORS erlaubt fuer Dev.
- Beim Moduswechsel wird `previous` zwischen Frontend und Backend synchronisiert.

## 4. Backend-Implementierung (Spring Boot)
1. Domain und DTOs anlegen:
- `DenominationCountDto { denomination, count }`
- `CalculationDto { amount, breakdown[] }`
- `DifferenceDto { denomination, delta }`
- `CalculateRequestDto { amount }`
- `CalculateResponseDto { amount, breakdown[], difference? }`
2. Fachlogik implementieren:
- `BreakdownService` (Greedy auf Integer-Cent)
- `DifferenceService` (Vergleich alte/neue Stueckelung)
- `CalculationStoreService` (letzte Berechnung lesen/schreiben)
3. REST Controller bereitstellen:
- `POST /calculate` berechnet fuer `amount`, liest `previous` aus Store, liefert `difference` und speichert Ergebnis als neue letzte Berechnung.
- `GET /calculations` liefert die aktuell gespeicherte letzte Berechnung inkl. Stueckelung.
- `POST /calculations` uebernimmt eine vom Frontend gesendete Berechnung in den Backend-Store.
- `GET /denominations` liefert verfuegbare Einheiten.
4. Validierung und Fehlerbehandlung:
- Bean Validation (Betrag > 0, max. 2 Nachkommastellen)
- Einheitliches Fehlerformat mit `@ControllerAdvice`
5. Backend-Tests mit JUnit:
- Unit-Tests (JUnit 5) fuer `BreakdownService`, `DifferenceService`, `CalculationStoreService`.
- API-Endpunkt-Tests (JUnit 5 + Spring MockMvc):
- `POST /calculate` mit erstem Aufruf (ohne Difference) und Folgeaufruf (mit Difference).
- `GET /calculations` mit und ohne gespeicherte Berechnung.
- `POST /calculations` speichert uebergebenes Ergebnis korrekt.
- `GET /denominations` liefert vollstaendige, sortierte Liste.
- Integrations-Tests (JUnit 5 + `@SpringBootTest`) fuer End-to-End-Flow im Backend.

## 5. Frontend-Implementierung (Angular 21 + PrimeNG)
1. Angular-21-App initialisieren (`frontend/`) und PrimeNG integrieren.
2. PrimeNG-Bausteine verwenden:
- `InputNumber` (EUR Betrag)
- `SelectButton` oder `ToggleButton` (Frontend/Backend-Modus)
- `Button` (Berechnen)
- `Table` (Stueckelung und Differenz)
- `Slider` (manuelle Aenderung der Stueckelung je Schein/Muenze)
- `Message`/`Toast` (Validierungs- und Fehlermeldungen)
- `Card`/`Panel` fuer strukturierte Darstellung
3. State-Handling:
- `currentResult`, `previousResult`, `mode`, `loading`, `error`
- `previousResult` wird lokal gehalten und bei Moduswechsel synchronisiert.
4. Manuelle Stueckelung mit Slidern:
- Wenn `currentResult` vorhanden ist, wird ein Slider-Bereich pro Einheit angezeigt.
- Ueber den Slidern steht immer: `Noch zu stueckelnder Betrag: X,XX EUR`.
- Restbetrag wird live berechnet:
- `remaining = amountInCents - sum(denominationInCents * count)`.
- Slider-Regeln je Einheit:
- Minimum immer `0`.
- Dynamisches Maximum: `max = currentCount + floor(remaining / denomination)`.
- Dadurch kann ein Slider nur so weit erhoeht werden, dass der Restbetrag nicht negativ wird.
- Beispiel `amount=150.00`, initial `100x1 + 50x1`:
- Setzt Nutzer `100` von `1` auf `0`, dann `remaining = 100.00`.
- Danach kann Slider `50` um maximal `2` erhoeht werden, `20` um maximal `5`, usw.
- Optionaler CTA `Stueckelung uebernehmen` nur aktiv, wenn `remaining == 0`.
- Bei Uebernahme wird `currentResult.breakdown` aktualisiert; im Backend-Modus zusaetzlich mit `POST /calculations` persistiert.
5. Berechnungswege:
- Frontend-Modus: lokale Berechnungsfunktion mit derselben Denominationsliste wie im Backend.
- Backend-Modus: Aufruf `POST /calculate`.
6. Moduswechsel-Synchronisation:
- Wechsel Backend -> Frontend:
- Frontend ruft `GET /calculations` auf und setzt die gelieferte Berechnung als lokales `previousResult`.
- Wechsel Frontend -> Backend:
- Frontend ruft `POST /calculations` auf und uebergibt die lokal gespeicherte letzte Berechnung an das Backend.
7. Anzeige:
- Tabelle 1: neue Stueckelung (Einheit, Anzahl)
- Tabelle 2: Differenz zur vorherigen Berechnung (Einheit, Delta)
- Slider-Bereich: manuelle Anpassung je Einheit + Restbetrag-Anzeige.

## 6. API-Vertrag (Vorschlag)
Berechnung:
```http
POST /calculate
Content-Type: application/json
```

Request:
```json
{
  "amount": "234.23"
}
```

Response:
```json
{
  "amount": "234.23",
  "breakdown": [
    { "denomination": "200.00", "count": 1 },
    { "denomination": "20.00", "count": 1 },
    { "denomination": "10.00", "count": 1 },
    { "denomination": "2.00", "count": 2 },
    { "denomination": "0.20", "count": 1 },
    { "denomination": "0.02", "count": 1 },
    { "denomination": "0.01", "count": 1 }
  ],
  "difference": [
    { "denomination": "200.00", "delta": 1 },
    { "denomination": "20.00", "delta": -1 },
    { "denomination": "10.00", "delta": 1 },
    { "denomination": "5.00", "delta": -1 },
    { "denomination": "2.00", "delta": 2 },
    { "denomination": "0.20", "delta": 0 },
    { "denomination": "0.10", "delta": -1 },
    { "denomination": "0.02", "delta": 0 },
    { "denomination": "0.01", "delta": 1 }
  ]
}
```

Synchronisation:
```http
GET /calculations
POST /calculations
```

`POST /calculations` Request:
```json
{
  "amount": "45.32",
  "breakdown": [
    { "denomination": "20.00", "count": 2 },
    { "denomination": "5.00", "count": 1 },
    { "denomination": "0.20", "count": 1 },
    { "denomination": "0.10", "count": 1 },
    { "denomination": "0.02", "count": 1 }
  ]
}
```

## 7. Teststrategie
1. Backend (JUnit 5):
- Unit-Tests fuer Berechnung, Differenz und Store.
- API-Endpunkt-Tests mit MockMvc fuer `/calculate`, `/calculations`, `/denominations`.
- Integrations-Tests fuer kompletten Backend-Flow inklusive gespeicherter letzter Berechnung.
2. Frontend E2E (Playwright):
- Happy Path: Betrag eingeben, berechnen, Stueckelung sichtbar.
- Folgeaufruf im gleichen Modus zeigt Difference zur vorherigen Berechnung.
- Moduswechsel Backend -> Frontend triggert `GET /calculations` und uebernimmt `previousResult`.
- Moduswechsel Frontend -> Backend triggert `POST /calculations` und Backend verwendet diesen Stand bei naechster Berechnung.
- Fehlerfall-Tests fuer API-Fehler/Timeout und leere gespeicherte Berechnung.
- Slider-Test: Reduktion einer Einheit erhoeht Restbetrag korrekt (z. B. `100` von `1` auf `0` bei `150.00` -> Rest `100.00`).
- Slider-Test: dynamische Maximalwerte passen sich an Restbetrag an (z. B. `50` max +2, `20` max +5 bei Rest `100.00`).
- Slider-Test: Uebernahme nur bei `remaining == 0`; bei Rest > 0 ist CTA deaktiviert.

## 8. Umsetzungsreihenfolge
1. Backend-Fachlogik + JUnit-Unit-Tests fertigstellen.
2. Backend-Store fuer letzte Berechnung implementieren.
3. REST API (`/calculate`, `/calculations`, `/denominations`) + Validierung bauen.
4. JUnit-API-Endpunkt- und Integrations-Tests fuer REST-Flows erstellen.
5. Angular-21-Projekt + PrimeNG Setup erstellen.
6. UI fuer Eingabe, Modus-Umschaltung und Ergebnisansicht bauen.
7. Frontend-Modus und Backend-Modus inkl. Synchronisation beim Moduswechsel integrieren.
8. Slider-basierte manuelle Stueckelungsanpassung inkl. Restbetragslogik integrieren.
9. Playwright-E2E-Tests fuer Haupt-, Wechsel- und Slider-Szenarien erstellen.
10. README um Startanleitung und Testbefehle (`mvn test`, `npx playwright test`) erweitern.

## 9. Akzeptanzkriterien
- Greedy-Stueckelung liefert fuer alle Testfaelle korrekte Anzahl je Einheit.
- Difference wird immer gegen die letzte Berechnung des aktiven Modus gebildet.
- Moduswechsel synchronisiert `previous` korrekt ueber `GET /calculations` bzw. `POST /calculations`.
- Bei vorhandener Stueckelung kann der Nutzer die Anzahl je Einheit per Slider manuell aendern.
- Restbetrag und Slider-Maxima aktualisieren sich konsistent und verhindern negative Restwerte.
- PrimeNG ist fuer die sichtbaren UI-Bausteine eingesetzt.
- Backend-API-Endpunkte sind mit JUnit getestet.
- Frontend-E2E-Szenarien sind mit Playwright automatisiert.
