# LagerClient - Artikelverwaltung

Eine Java-Desktop-Anwendung zur Verwaltung von Lagerartikeln mit benutzerfreundlicher UI, Echtzeit-Formatierung und C#-Backend-Anbindung über HTTP.

![Java Version](https://img.shields.io/badge/Java-21-orange)
![Status](https://img.shields.io/badge/Status-Stable-green)

## Features

- **Artikelverwaltung**: Anzeigen, Hinzufügen, Bearbeiten und Löschen von Lagerartikeln
- **Formatierung**: Zelleninhalte können fett, kursiv und farblich formatiert werden
- **Konfliktmanagement**: Erkennung und Auflösung von Bearbeitungskonflikten bei gleichzeitiger Nutzung
- **Konfigurierbarkeit**: Externe Konfigurationsdatei für UI-Einstellungen und API-Verbindung
- **Responsive Design**: Moderne Benutzeroberfläche mit Unterstützung für Sortierung und Filterung

## Screenshot
![image](https://github.com/user-attachments/assets/bcd121c0-f7dc-42c0-945e-627ea105e957)

## Installation

### Voraussetzungen
- Java 21 oder höher
- Zugang zur Backend-API (standardmäßig auf https://localhost:5001/api/article)

### Installation und Start
1. Lade die [neueste Version](https://github.com/l-striegel/LagerClientJava/releases) herunter
2. Entpacke alle Dateien in ein Verzeichnis deiner Wahl
3. Passe bei Bedarf die `config.properties` Datei an
4. Starte die Anwendung über die `start.bat` (Windows) oder über den Befehl:
   ```
   java -jar lagerclient-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

## Konfiguration

Die Anwendung wird über die `config.properties` Datei konfiguriert, die im selben Verzeichnis wie die JAR-Datei liegen muss:

| Einstellung | Beschreibung | Standardwert |
|-------------|--------------|--------------|
| api.url | URL der Backend-API | https://localhost:5001/api/article |
| app.debug | Debug-Modus aktivieren | false |
| ui.table.rowheight | Zeilenhöhe der Tabelle | 25 |
| ui.table.zebracolor | Farbe für Zebrastreifen | #F0F0F0 |

## Architektur

Die Anwendung ist nach dem MVC-Muster strukturiert:

- **Model**: Repräsentiert Lagerartikel und ihre Eigenschaften (`Article`, `ArticleTableModel`)
- **View**: Swing-basierte Benutzeroberfläche (`LagerClientApp`)
- **Controller**: API-Kommunikation und Datenmanagement (`ApiClient`)

Zusätzlich gibt es eine zentrale Konfigurationsverwaltung mit der `AppConfig`-Klasse, die für flexible und wartbare Einstellungen sorgt.

## Technologie-Stack

- **Java 21**: Moderne Sprachfeatures und Performance
- **Swing**: Native Benutzeroberfläche
- **Jackson**: JSON-Verarbeitung für API-Kommunikation
- **SLF4J & Logback**: Strukturiertes Logging
- **Maven**: Build-Management und Dependency-Verwaltung

## Entwicklung

### Voraussetzungen für die Entwicklung
- Java Development Kit (JDK) 21
- Maven
- IDE (empfohlen: IntelliJ IDEA oder Eclipse)

### Setup-Anweisungen
1. Repository klonen: `git clone https://github.com/l-striegel/LagerClientJava.git`
2. Projekt in der IDE öffnen
3. Maven-Dependencies installieren
4. Projekt bauen: `mvn clean package`

## Lizenz

Dieses Projekt ist für Bewerbungszwecke erstellt und unterliegt keiner spezifischen Lizenz.
