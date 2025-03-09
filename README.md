# LagerClient - Artikelverwaltung

Eine Java-Desktop-Anwendung zur Verwaltung von Lagerartikeln mit benutzerfreundlicher UI, Echtzeit-Formatierung und C#-Backend-Anbindung über HTTP.

![Java Version](https://img.shields.io/badge/Java-21-orange)
![Status](https://img.shields.io/badge/Status-Stable-green)
![Version](https://img.shields.io/badge/Version-1.1.0-blue)

## Features

- **Artikelverwaltung**: Anzeigen, Hinzufügen, Bearbeiten und Löschen von Lagerartikeln
- **Formatierung**: Zelleninhalte können fett, kursiv und farblich formatiert werden
- **Konfliktmanagement**: Erkennung und Auflösung von Bearbeitungskonflikten bei gleichzeitiger Nutzung
- **Konfigurierbarkeit**: Externe Konfigurationsdatei für UI-Einstellungen und API-Verbindung
- **Responsive Design**: Moderne Benutzeroberfläche mit Unterstützung für Sortierung und Filterung
- **Offline-Modus**: Lokale Datenspeicherung zur Verwendung ohne Backend-Verbindung

## Screenshot
![image](https://github.com/user-attachments/assets/26e143af-49b9-4631-b6ff-6052913ffd18)
![image](https://github.com/user-attachments/assets/1ac19bb0-1f98-41f2-a1fa-a61bd52a9732)


## Installation

### Voraussetzungen
- Java 21 oder höher
- Zugang zur Backend-API (standardmäßig auf https://localhost:5001/api/article) oder Nutzung im Offline-Modus

### Installation und Start
1. Lade die [neueste Version](https://github.com/l-striegel/LagerClientJava/releases/latest) herunter
2. Entpacke die ZIP-Datei und lege die `lagerclient.jar` und `setup.bat` im selben Verzeichnis ab
3. Führe die `setup.bat` aus, um die Anwendung zu installieren
   - Dies erstellt automatisch alle notwendigen Verzeichnisse und Konfigurationsdateien
   - Es wird ein "LagerClient" Ordner im aktuellen Verzeichnis erstellt
   - Die Anwendung wird mit Beispieldaten für den Offline-Modus vorkonfiguriert
4. Nach der Installation kannst du die Anwendung starten durch:
   - Doppelklick auf die JAR-Datei im Installationsverzeichnis
   - Wenn du bei der Installation einer Desktop-Verknüpfung zugestimmt hast, kannst du diese verwenden
   - Alternativ: Nutze die mitinstallierte `start.bat` für bessere Fehlermeldungen und Debugging (zeigt Konsolenausgaben an)

## Konfiguration

Die Anwendung wird über die `config.properties` Datei konfiguriert, die während der Installation erstellt wird:

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

## Änderungshistorie

### v1.1.0
- Implementierung eines Offline-Modus mit lokaler Datenspeicherung
- Verbesserte Installation durch automatisiertes `setup.bat`-Skript
- Automatische Erstellung aller benötigten Verzeichnisse und Konfigurationsdateien
- Option zur Erstellung einer Desktop-Verknüpfung

### v1.0.1
- Hartcodierte URLs entfernt und durch AppConfig-Einstellungen ersetzt

### v1.0.0
- Erste stabile veröffentlichte Version

## Lizenz
Dieses Projekt ist für Bewerbungszwecke erstellt und unterliegt keiner spezifischen Lizenz.

Icon-Attributionen:
- In-stock icons created by Freepik - Flaticon (https://www.flaticon.com/free-icons/in-stock)
