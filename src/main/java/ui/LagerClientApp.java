package ui;

import api.ApiClient;
import config.AppConfig;
import model.Article;
import model.ArticleTableModel;
import model.CellStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.List;

/**
 * Hauptklasse der Anwendung, die die Benutzeroberfläche und die Anwendungslogik enthält.
 */
public class LagerClientApp {
    private static final Logger logger = LoggerFactory.getLogger(LagerClientApp.class);

    private JTable table;
    private ArticleTableModel tableModel;
    private List<Article> articles;
    private final Set<Article> changedArticles = new HashSet<>();
    private TableRowSorter<ArticleTableModel> sorter;
    private Point dragStart;
    private final Set<Point> selectedCells = new HashSet<>();
    private JFrame mainFrame;
    private final Map<Integer, String> originalTimestamps = new HashMap<>();

    /**
     * Einstiegspunkt der Anwendung.
     *
     * @param args Kommandozeilenargumente
     */
    public static void main(String[] args) {
        logger.info("Anwendung wird gestartet");
        SwingUtilities.invokeLater(() -> {
            try {
                new LagerClientApp().createAndShowGUI();
                logger.info("Benutzeroberfläche erfolgreich gestartet");
            } catch (Exception e) {
                logger.error("Fehler beim Starten der Benutzeroberfläche: {}", e.getMessage(), e);
                JOptionPane.showMessageDialog(null,
                        "Fehler beim Starten der Anwendung: " + e.getMessage(),
                        "Startfehler",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Erstellt und zeigt die Benutzeroberfläche an.
     */
    /**
     * Erstellt und zeigt die Benutzeroberfläche an.
     */
    private void createAndShowGUI() {
        logger.debug("Erstelle Benutzeroberfläche");
        mainFrame = new JFrame("LagerClient - Artikelverwaltung");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(900, 500);

        // Referenz auf diese Instanz speichern, damit der Renderer darauf zugreifen kann
        mainFrame.getRootPane().putClientProperty("appInstance", this);
        logger.trace("appInstance in RootPane gespeichert");

        logger.info("Lade Artikel von der API");
        articles = ApiClient.fetchArticles();
        logger.debug("{} Artikel geladen", articles.size());

        // Speichere ursprüngliche Timestamps
        for (Article article : articles) {
            originalTimestamps.put(article.id, article.timestamp);
        }
        logger.trace("Ursprüngliche Timestamps gespeichert");

        // Erstelle Tabellenmodell und -komponente
        tableModel = new ArticleTableModel(articles, changedArticles);
        table = new JTable(tableModel);
        table.setCellSelectionEnabled(false);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Verwende AppConfig für Tabellenkonfiguration
        AppConfig config = AppConfig.getInstance();
        table.setRowHeight(config.getTableRowHeight());
        logger.debug("Tabellenzeilenhöhe auf {} gesetzt", config.getTableRowHeight());

        logger.debug("Tabelle mit {} Spalten erstellt", table.getColumnCount());

        // Konfiguriere Sortierung und Rendering
        sorter.setComparator(3, Comparator.comparingInt(o -> Integer.parseInt(o.toString())));
        table.setDefaultRenderer(Object.class, new StyledCellRenderer(articles));
        table.addMouseListener(new TableMouseListener(table, articles, changedArticles));

        // Layout und UI-Komponenten
        JScrollPane scrollPane = new JScrollPane(table);
        mainFrame.add(scrollPane, BorderLayout.CENTER);
        logger.trace("ScrollPane zur Tabelle hinzugefügt");

        createMenuBar(mainFrame);
        addSelectionListener();
        createButtons();

        mainFrame.setVisible(true);
        logger.info("Benutzeroberfläche wurde angezeigt");
    }

    /**
     * Erstellt die Schaltflächen und fügt sie der Benutzeroberfläche hinzu.
     */
    private void createButtons() {
        JButton saveButton = new JButton("Änderungen speichern");
        saveButton.addActionListener(e -> saveChanges());

        // Debug-Button nur anzeigen, wenn Debug-Modus aktiviert ist
        boolean debugMode = AppConfig.getInstance().isDebugMode();
        logger.debug("Debug-Modus aus Konfiguration geladen: {}", debugMode);

        // Neuer Button zum Hinzufügen eines Artikels
        JButton addButton = new JButton("Neuer Artikel");
        addButton.addActionListener(e -> addNewArticle());

        // Neuer Button zum Löschen eines Artikels
        JButton deleteButton = new JButton("Artikel löschen");
        deleteButton.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                deleteArticle(modelRow);
            } else {
                logger.warn("Kein Artikel zum Löschen ausgewählt");
                JOptionPane.showMessageDialog(null, "Bitte wählen Sie einen Artikel zum Löschen aus.");
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);

        // Debug-Button nur hinzufügen, wenn Debug-Modus aktiviert ist
        if (debugMode) {
            JButton debugButton = new JButton("Debug: Markierte Zellen");
            debugButton.addActionListener(e -> printSelectedCells());
            buttonPanel.add(debugButton);
            logger.debug("Debug-Button wurde hinzugefügt");
        }

        mainFrame.add(buttonPanel, BorderLayout.SOUTH);
        logger.debug("Button-Panel hinzugefügt");
    }

    /**
     * Getter für selectedCells, damit der Renderer darauf zugreifen kann.
     *
     * @return Set mit ausgewählten Zellen als Points (x=row, y=column)
     */
    public Set<Point> getSelectedCells() {
        return selectedCells;
    }

    /**
     * Erstellt die Menüleiste mit Formatierungsoptionen.
     *
     * @param frame Das JFrame, zu dem die Menüleiste hinzugefügt wird
     */
    private void createMenuBar(JFrame frame) {
        logger.debug("Erstelle Menüleiste");
        JMenuBar menuBar = new JMenuBar();

        JButton boldButton = new JButton("Fett");
        boldButton.addActionListener(e -> {
            logger.debug("Fett-Schaltfläche geklickt");
            applyFormatting("bold");
        });

        JButton italicButton = new JButton("Kursiv");
        italicButton.addActionListener(e -> {
            logger.debug("Kursiv-Schaltfläche geklickt");
            applyFormatting("italic");
        });

        JButton colorButton = new JButton("Farbe");
        colorButton.addActionListener(e -> {
            logger.debug("Farbe-Schaltfläche geklickt");
            changeCellColor();
        });

        menuBar.add(boldButton);
        menuBar.add(italicButton);
        menuBar.add(colorButton);

        frame.setJMenuBar(menuBar);
        logger.debug("Menüleiste hinzugefügt");
    }

    /**
     * Fügt Listener für Mausaktionen hinzu, um Zellenauswahl zu ermöglichen.
     */
    private void addSelectionListener() {
        logger.debug("Füge Selektions-Listener zur Tabelle hinzu");

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point clickedPoint = e.getPoint();
                int row = table.rowAtPoint(clickedPoint);
                int col = table.columnAtPoint(clickedPoint);

                if (row < 0 || col < 0) {
                    logger.trace("Mausklick außerhalb gültiger Zellen: {},{}", row, col);
                    return;
                }

                // STRG + Klick: Einzelne Zelle zur Auswahl hinzufügen oder entfernen
                if (e.isControlDown()) {
                    Point cell = new Point(row, col);
                    if (selectedCells.contains(cell)) {
                        logger.trace("Entferne Zelle aus Auswahl: {},{}", row, col);
                        selectedCells.remove(cell);
                    } else {
                        logger.trace("Füge Zelle zur Auswahl hinzu: {},{}", row, col);
                        selectedCells.add(cell);
                    }
                    table.repaint();
                    return;
                }

                // Normale Auswahl (Shift für Bereichsauswahl)
                if (!e.isShiftDown()) {
                    logger.trace("Leere Zellenauswahl");
                    selectedCells.clear();
                }

                // Bei normaler Auswahl füge die aktuelle Zelle hinzu
                logger.trace("Füge Zelle zur Auswahl hinzu: {},{}", row, col);
                selectedCells.add(new Point(row, col));
                dragStart = clickedPoint;
                table.repaint();
            }
        });

        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int startRow = table.rowAtPoint(dragStart);
                int startCol = table.columnAtPoint(dragStart);
                int endRow = table.rowAtPoint(e.getPoint());
                int endCol = table.columnAtPoint(e.getPoint());

                logger.trace("Ziehen von {},{} nach {},{}", startRow, startCol, endRow, endCol);

                if (!e.isShiftDown() && !e.isControlDown()) {
                    selectedCells.clear();
                }

                for (int row = Math.min(startRow, endRow); row <= Math.max(startRow, endRow); row++) {
                    for (int col = Math.min(startCol, endCol); col <= Math.max(startCol, endCol); col++) {
                        if (row >= 0 && col >= 0) {
                            selectedCells.add(new Point(row, col));
                        }
                    }
                }
                table.repaint();
            }
        });

        logger.debug("Selektions-Listener hinzugefügt");
    }

    /**
     * Speichert Änderungen an Artikeln mit Konfliktprüfung.
     */
    private void saveChanges() {
        logger.info("Speichere Änderungen");

        if (changedArticles.isEmpty()) {
            logger.info("Keine Änderungen zu speichern");
            JOptionPane.showMessageDialog(null, "Keine Änderungen zu speichern.");
            return;
        }

        logger.debug("{} geänderte Artikel gefunden", changedArticles.size());

        // Sammle Artikel, die einen Konflikt haben könnten
        List<Article> conflictedArticles = new ArrayList<>();

        // Prüfe jeden geänderten Artikel auf Konflikte
        for (Article article : changedArticles) {
            try {
                // Lade den aktuellen Zustand des Artikels aus der Datenbank
                int apiId = article.id;
                logger.debug("Prüfe auf Konflikte für Artikel ID {}", apiId);

                Article currentDbArticle = ApiClient.fetchArticle(apiId);

                // Vergleiche den Timestamp mit dem Original-Timestamp
                String originalTimestamp = originalTimestamps.get(apiId);
                if (!currentDbArticle.timestamp.equals(originalTimestamp)) {
                    logger.warn("Konflikt bei Artikel ID {}: Timestamps unterschiedlich", apiId);
                    logger.debug("Original: {}, Aktuell: {}", originalTimestamp, currentDbArticle.timestamp);
                    conflictedArticles.add(currentDbArticle);
                }
            } catch (Exception ex) {
                logger.error("Fehler beim Prüfen auf Konflikte für Artikel ID {}: {}",
                        article.id, ex.getMessage(), ex);
            }
        }

        // Wenn Konflikte vorhanden sind, frage den Benutzer
        if (!conflictedArticles.isEmpty()) {
            logger.info("{} Konflikte gefunden", conflictedArticles.size());
            handleConflicts(conflictedArticles);
        } else {
            logger.info("Keine Konflikte gefunden, speichere Änderungen direkt");
            saveChangesForced(changedArticles);
        }
    }

    /**
     * Behandelt Konflikte zwischen lokalen und Datenbankversionen von Artikeln.
     *
     * @param conflictedArticles Liste der Artikel mit Konflikten
     */
    private void handleConflicts(List<Article> conflictedArticles) {
        StringBuilder conflictMessage = new StringBuilder("Folgende Artikel wurden von anderen Benutzern geändert:\n\n");

        for (Article conflict : conflictedArticles) {
            Article localVersion = null;
            for (Article changed : changedArticles) {
                if (changed.id == conflict.id) {
                    localVersion = changed;
                    break;
                }
            }

            if (localVersion != null) {
                conflictMessage.append("Artikel #").append(conflict.id)
                        .append(" (").append(conflict.name).append(")\n");

                logger.debug("Bereite Konfliktmeldung für Artikel ID {} vor", conflict.id);

                // Vergleiche Felder und füge Unterschiede zur Meldung hinzu
                appendFieldDifferences(conflictMessage, localVersion, conflict);
                conflictMessage.append("\n");
            }
        }

        conflictMessage.append("Wie möchten Sie fortfahren?\n");

        String[] options = {"Meine Änderungen überschreiben", "Änderungen aus der DB übernehmen", "Abbrechen"};
        int choice = JOptionPane.showOptionDialog(null,
                conflictMessage.toString(),
                "Konflikte erkannt",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[2]);

        logger.info("Benutzer hat Option {} für Konflikte gewählt",
                choice >= 0 && choice < options.length ? options[choice] : "Abbrechen");

        if (choice == 0) {
            // Benutzer will seine Änderungen durchsetzen
            logger.info("Benutzer erzwingt eigene Änderungen");
            saveChangesForced(changedArticles);
        } else if (choice == 1) {
            // Benutzer will DB-Änderungen übernehmen
            logger.info("Benutzer übernimmt DB-Änderungen");
            updateLocalArticles(conflictedArticles);
        } else {
            // Benutzer bricht ab
            logger.info("Benutzer hat Speichern abgebrochen");
        }
    }

    /**
     * Fügt Unterschiede zwischen lokaler und Datenbankversion eines Artikels zur Meldung hinzu.
     *
     * @param message Die StringBuilder-Instanz für die Meldung
     * @param local Die lokale Version des Artikels
     * @param db Die Datenbankversion des Artikels
     */
    private void appendFieldDifferences(StringBuilder message, Article local, Article db) {
        if (!local.name.equals(db.name)) {
            message.append("- Name: ").append(local.name).append(" => ").append(db.name).append("\n");
        }
        if (!local.type.equals(db.type)) {
            message.append("- Typ: ").append(local.type).append(" => ").append(db.type).append("\n");
        }
        if (local.stock != db.stock) {
            message.append("- Bestand: ").append(local.stock).append(" => ").append(db.stock).append("\n");
        }
        if (!local.unit.equals(db.unit)) {
            message.append("- Einheit: ").append(local.unit).append(" => ").append(db.unit).append("\n");
        }
        if (local.price != db.price) {
            message.append("- Preis: ").append(local.price).append(" => ").append(db.price).append("\n");
        }
        if (!local.location.equals(db.location)) {
            message.append("- Lagerplatz: ").append(local.location).append(" => ").append(db.location).append("\n");
        }
        if (!local.status.equals(db.status)) {
            message.append("- Status: ").append(local.status).append(" => ").append(db.status).append("\n");
        }
        if (!local.link.equals(db.link)) {
            message.append("- Link: ").append(local.link).append(" => ").append(db.link).append("\n");
        }

        // Hinweis auf mögliche Formatierungsänderungen
        if (!local.stylesJson.equals(db.stylesJson)) {
            message.append("- Formatierungen wurden ebenfalls geändert\n");
        }
    }

    /**
     * Speichert Änderungen an Artikeln ohne Konfliktprüfung.
     *
     * @param articlesToSave Die zu speichernden Artikel
     */
    private void saveChangesForced(Set<Article> articlesToSave) {
        logger.info("Speichere {} Artikel zwangsweise", articlesToSave.size());

        for (Article article : articlesToSave) {
            try {
                // Stelle sicher, dass keine null-Werte in den Styles gesendet werden
                if (article.styles != null) {
                    for (Map.Entry<String, CellStyle> entry : article.styles.entrySet()) {
                        CellStyle style = entry.getValue();
                        // Wenn color null ist, setze einen Standardwert
                        if (style.color == null) {
                            style.color = "#000000"; // Schwarze Standardfarbe
                        }
                    }
                }

                // Timestamp im Format aktualisieren, das vom Server akzeptiert wird
                Instant now = Instant.now();
                article.timestamp = now.toString().split("\\.")[0] + "Z"; // Format: 2025-03-07T16:22:25Z
                logger.debug("Artikel ID {}: Timestamp aktualisiert auf {}", article.id, article.timestamp);

                int apiId = article.id;
                String apiUrl = AppConfig.getInstance().getApiUrl();
                URL putUrl = new URL(apiUrl + "/" + apiId);
                HttpURLConnection putConn = (HttpURLConnection) putUrl.openConnection();
                putConn.setRequestMethod("PUT");
                putConn.setRequestProperty("Content-Type", "application/json");
                putConn.setDoOutput(true);

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                String jsonPayload = objectMapper.writeValueAsString(article);
                logger.debug("Sende PUT-Payload für Artikel ID {}: {}", apiId, jsonPayload);

                try (OutputStream os = putConn.getOutputStream()) {
                    os.write(jsonPayload.getBytes());
                    os.flush();
                }

                int responseCode = putConn.getResponseCode();
                logger.debug("PUT-Antwortcode für Artikel ID {}: {}", apiId, responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    // Aktualisiere den gespeicherten Original-Timestamp
                    originalTimestamps.put(article.id, article.timestamp);
                    logger.info("Artikel ID {} erfolgreich gespeichert", apiId);
                } else {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(
                            putConn.getErrorStream() != null ? putConn.getErrorStream() : putConn.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }
                        logger.error("Fehlerantwort vom Server für Artikel ID {}: {}", apiId, response.toString());

                        // Fehler dem Benutzer anzeigen
                        JOptionPane.showMessageDialog(null,
                                "Fehler beim Speichern von Artikel #" + article.id + ": " + response.toString(),
                                "Fehler",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                putConn.disconnect();
            } catch (Exception ex) {
                logger.error("Ausnahme beim Speichern von Artikel ID {}: {}", article.id, ex.getMessage(), ex);
                JOptionPane.showMessageDialog(null,
                        "Fehler beim Speichern: " + ex.getMessage(),
                        "Fehler",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        changedArticles.clear();
        logger.info("Alle Änderungen erfolgreich gespeichert");
        JOptionPane.showMessageDialog(null, "Alle Änderungen gespeichert!", "Erfolg", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Aktualisiert lokale Artikel mit den Versionen aus der Datenbank.
     *
     * @param dbArticles Die Artikel aus der Datenbank
     */
    private void updateLocalArticles(List<Article> dbArticles) {
        logger.info("Aktualisiere lokale Artikel mit DB-Versionen: {} Artikel", dbArticles.size());

        for (Article dbArticle : dbArticles) {
            int modelIndex = tableModel.getModelIndexFromApiId(dbArticle.id);
            if (modelIndex >= 0) {
                logger.debug("Aktualisiere lokalen Artikel ID {} an Position {}", dbArticle.id, modelIndex);

                // Ersetze den lokalen Artikel mit der DB-Version
                articles.set(modelIndex, dbArticle);

                // Aktualisiere den gespeicherten Original-Timestamp
                originalTimestamps.put(dbArticle.id, dbArticle.timestamp);

                // Entferne den Artikel aus den geänderten Artikeln
                changedArticles.removeIf(a -> a.id == dbArticle.id);
            } else {
                logger.warn("Konnte Modellindex für Artikel ID {} nicht finden", dbArticle.id);
            }
        }

        tableModel.fireTableDataChanged();
        logger.info("Lokale Daten mit DB-Änderungen aktualisiert");
        JOptionPane.showMessageDialog(null, "Lokale Daten wurden mit Datenbankänderungen aktualisiert.",
                "Erfolg", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addNewArticle() {
        logger.info("Erstelle neuen Artikel");

        // Erstelle einen Dialog zum Eingeben der Artikeldaten
        JDialog dialog = new JDialog(mainFrame, "Neuen Artikel erstellen", true);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(mainFrame);

        // Erstelle die Eingabefelder
        JTextField nameField = new JTextField("Neuer Artikel", 20);
        JTextField typeField = new JTextField("Elektronik", 20);
        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        JTextField unitField = new JTextField("Stück", 20);
        JSpinner priceSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 9999999.99, 0.01));
        JTextField locationField = new JTextField("", 20);
        JTextField statusField = new JTextField("Auf Lager", 20);
        JTextField linkField = new JTextField("", 20);

        // Füge die Felder zum Dialog hinzu
        dialog.add(new JLabel("Name (max. 100 Zeichen):"));
        dialog.add(nameField);
        dialog.add(new JLabel("Typ (max. 50 Zeichen):"));
        dialog.add(typeField);
        dialog.add(new JLabel("Bestand:"));
        dialog.add(stockSpinner);
        dialog.add(new JLabel("Einheit (max. 20 Zeichen):"));
        dialog.add(unitField);
        dialog.add(new JLabel("Preis:"));
        dialog.add(priceSpinner);
        dialog.add(new JLabel("Lagerort (max. 100 Zeichen):"));
        dialog.add(locationField);
        dialog.add(new JLabel("Status (max. 50 Zeichen):"));
        dialog.add(statusField);
        dialog.add(new JLabel("Link (gültige URL mit http:// oder https://, max. 255 Zeichen):"));
        dialog.add(linkField);

        // Buttons zum Speichern oder Abbrechen
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Speichern");
        JButton cancelButton = new JButton("Abbrechen");

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        dialog.add(new JLabel(""));
        dialog.add(buttonPanel);

        // Abbrechen-Button-Aktion
        cancelButton.addActionListener(e -> dialog.dispose());

        // Speichern-Button-Aktion
        saveButton.addActionListener(e -> {
            Article newArticle = new Article();
            newArticle.name = nameField.getText().trim();
            newArticle.type = typeField.getText().trim();
            newArticle.stock = (int) stockSpinner.getValue();
            newArticle.unit = unitField.getText().trim();
            newArticle.price = ((Number) priceSpinner.getValue()).doubleValue();
            newArticle.location = locationField.getText().trim();
            newArticle.status = statusField.getText().trim();
            newArticle.link = linkField.getText().trim();

            // Wichtig: Timestamp setzen, da das Backend DateTime erwartet
            newArticle.timestamp = Instant.now().toString();
            newArticle.styles = new HashMap<>();

            // Validierung
            if (!newArticle.isValid()) {
                JOptionPane.showMessageDialog(dialog,
                        "Bitte überprüfen Sie die Eingaben. Stellen Sie sicher, dass alle Pflichtfelder ausgefüllt sind\n" +
                                "und Links dem Format http://example.com entsprechen.",
                        "Validierungsfehler",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Senden des Artikels an den Server
            try {
                String apiUrl = AppConfig.getInstance().getApiUrl();
                URL postUrl = new URL(apiUrl);
                HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
                postConn.setRequestMethod("POST");
                postConn.setRequestProperty("Content-Type", "application/json");
                postConn.setDoOutput(true);

                // Konfiguriere ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                // Article-Objekt direkt senden
                String jsonPayload = objectMapper.writeValueAsString(newArticle);

                // Debug: Zeige das gesendete JSON an
                logger.debug("Sende POST-Payload: {}", jsonPayload);

                try (OutputStream os = postConn.getOutputStream()) {
                    os.write(jsonPayload.getBytes());
                    os.flush();
                }

                // Verarbeite die Antwort und aktualisiere die Tabelle
                int responseCode = postConn.getResponseCode();
                logger.debug("POST-Antwortcode: {}", responseCode);

                // Bei Fehler: Lese den Fehlertext aus
                if (responseCode != HttpURLConnection.HTTP_CREATED && responseCode != HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(
                            postConn.getErrorStream() != null ? postConn.getErrorStream() : postConn.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }
                        String errorText = response.toString();
                        logger.error("Fehlerantwort vom Server: {}", errorText);
                        JOptionPane.showMessageDialog(dialog,
                                "Fehler beim Erstellen des Artikels: " + responseCode +
                                        "\nDetails: " + errorText,
                                "Fehler",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    logger.info("Artikel erfolgreich erstellt, lade Artikelliste neu");
                    dialog.dispose();

                    // Lade alle Artikel neu
                    articles = ApiClient.fetchArticles();
                    // Speichere die ursprünglichen Timestamps für die neuen Artikel
                    for (Article article : articles) {
                        originalTimestamps.put(article.id, article.timestamp);
                    }
                    tableModel = new ArticleTableModel(articles, changedArticles);
                    table.setModel(tableModel);

                    // Setze den Sorter und Renderer erneut
                    sorter = new TableRowSorter<>(tableModel);
                    table.setRowSorter(sorter);
                    sorter.setComparator(3, Comparator.comparingInt(o -> Integer.parseInt(o.toString())));

                    table.setDefaultRenderer(Object.class, new StyledCellRenderer(articles));

                    // Initialisiere das ID-Mapping neu
                    tableModel.refreshIdMapping();

                    logger.info("Benutzeroberfläche nach Artikelerstellung aktualisiert");
                    JOptionPane.showMessageDialog(null, "Neuer Artikel wurde erstellt.", "Erfolg", JOptionPane.INFORMATION_MESSAGE);
                }

                postConn.disconnect();
            } catch (Exception ex) {
                logger.error("Ausnahme beim Erstellen eines neuen Artikels: {}", ex.getMessage(), ex);
                JOptionPane.showMessageDialog(dialog, "Fehler beim Erstellen des Artikels: " + ex.getMessage(),
                        "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    /**
     * Löscht einen Artikel.
     *
     * @param modelRow Die Zeile des zu löschenden Artikels im Modell
     */
    private void deleteArticle(int modelRow) {
        if (modelRow < 0 || modelRow >= articles.size()) {
            logger.warn("Versuch, ungültigen Artikel zu löschen: Zeile {}", modelRow);
            JOptionPane.showMessageDialog(null, "Ungültiger Artikel ausgewählt.",
                    "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Article article = articles.get(modelRow);
        int apiId = article.id;
        int displayId = modelRow + 1;

        logger.info("Löschen von Artikel ID {} ({}) angefordert", apiId, article.name);

        // Bestätigungsdialog
        int confirm = JOptionPane.showConfirmDialog(null,
                "Möchten Sie Artikel #" + displayId + " (" + article.name + ") wirklich löschen?",
                "Artikel löschen",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            logger.info("Benutzer hat Löschen von Artikel ID {} abgebrochen", apiId);
            return;
        }

        // API-Aufruf zum Löschen
        try {
            String apiUrl = AppConfig.getInstance().getApiUrl();
            URL deleteUrl = new URL(apiUrl + "/" + apiId);
            HttpURLConnection deleteConn = (HttpURLConnection) deleteUrl.openConnection();
            deleteConn.setRequestMethod("DELETE");

            logger.debug("Sende DELETE-Anfrage für Artikel ID {}", apiId);

            int responseCode = deleteConn.getResponseCode();
            logger.debug("DELETE-Antwortcode: {}", responseCode);
            deleteConn.disconnect();

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                // Entferne den Artikel aus der lokalen Liste
                articles.remove(modelRow);

                // Entferne den Artikel aus den Original-Timestamps
                originalTimestamps.remove(apiId);

                // Aktualisiere das TableModel und das ID-Mapping
                tableModel.fireTableDataChanged();
                tableModel.refreshIdMapping();

                // Leere die ausgewählten Zellen, da sich die Indizes verschoben haben
                selectedCells.clear();

                logger.info("Artikel ID {} erfolgreich gelöscht", apiId);
                JOptionPane.showMessageDialog(null, "Artikel erfolgreich gelöscht.",
                        "Erfolg", JOptionPane.INFORMATION_MESSAGE);
            } else {
                logger.error("Fehler beim Löschen von Artikel ID {}: HTTP-Code {}", apiId, responseCode);
                JOptionPane.showMessageDialog(null, "Fehler beim Löschen des Artikels: " + responseCode,
                        "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            logger.error("Ausnahme beim Löschen von Artikel ID {}: {}", apiId, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, "Fehler beim Löschen des Artikels: " + ex.getMessage(),
                    "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Wendet Formatierung (fett oder kursiv) auf ausgewählte Zellen an.
     *
     * @param styleType Der Typ der Formatierung ("bold" oder "italic")
     */
    private void applyFormatting(String styleType) {
        logger.info("Wende Formatierung '{}' auf {} ausgewählte Zellen an", styleType, selectedCells.size());

        for (Point cell : selectedCells) {
            int viewRow = cell.x;
            int col = cell.y;

            // Konvertiere View-Zeile zu Modell-Zeile
            int modelRow = table.convertRowIndexToModel(viewRow);
            if (modelRow < 0 || modelRow >= articles.size()) {
                logger.trace("Ungültige Zeile beim Formatieren: {}", modelRow);
                continue;
            }

            Article article = articles.get(modelRow);
            String columnName = table.getColumnName(col);

            logger.debug("Formatiere Zelle für Artikel ID {} in Spalte '{}'", article.id, columnName);

            if (article.styles == null) {
                article.styles = new HashMap<>();
            }
            article.styles.putIfAbsent(columnName, new CellStyle());

            CellStyle style = article.styles.get(columnName);

            if (styleType.equals("bold")) {
                style.bold = !style.bold;
                logger.debug("Bold-Status für Artikel ID {} Spalte '{}' geändert auf: {}",
                        article.id, columnName, style.bold);
            } else if (styleType.equals("italic")) {
                style.italic = !style.italic;
                logger.debug("Italic-Status für Artikel ID {} Spalte '{}' geändert auf: {}",
                        article.id, columnName, style.italic);
            }

            changedArticles.add(article);
        }

        logger.debug("Formatierung angewendet, Tabelle wird neu gezeichnet");
        table.repaint();
    }

    /**
     * Ändert die Textfarbe ausgewählter Zellen.
     */
    private void changeCellColor() {
        logger.info("Farbänderung für {} ausgewählte Zellen angefordert", selectedCells.size());

        Color newColor = JColorChooser.showDialog(null, "Wähle eine Farbe", Color.BLACK);
        if (newColor == null) {
            logger.debug("Farbauswahl abgebrochen");
            return;
        }

        logger.debug("Neue Farbe ausgewählt: RGB({},{},{})",
                newColor.getRed(), newColor.getGreen(), newColor.getBlue());

        for (Point cell : selectedCells) {
            int viewRow = cell.x;
            int col = cell.y;

            // Konvertiere View-Zeile zu Modell-Zeile
            int modelRow = table.convertRowIndexToModel(viewRow);
            if (modelRow < 0 || modelRow >= articles.size()) {
                logger.trace("Ungültige Zeile bei Farbänderung: {}", modelRow);
                continue;
            }

            Article article = articles.get(modelRow);
            String columnName = table.getColumnName(col);

            logger.debug("Ändere Farbe für Artikel ID {} in Spalte '{}'", article.id, columnName);

            if (article.styles == null) {
                article.styles = new HashMap<>();
            }
            article.styles.putIfAbsent(columnName, new CellStyle());

            CellStyle style = article.styles.get(columnName);
            String hexColor = String.format("#%02x%02x%02x",
                    newColor.getRed(), newColor.getGreen(), newColor.getBlue());
            style.color = hexColor;

            logger.debug("Farbe für Artikel ID {} Spalte '{}' auf {} gesetzt",
                    article.id, columnName, hexColor);

            changedArticles.add(article);
        }

        logger.debug("Farbänderung angewendet, Tabelle wird neu gezeichnet");
        table.repaint();
    }

    /**
     * Gibt Informationen über ausgewählte Zellen in der Konsole aus.
     * Primär für Debug-Zwecke.
     */
    private void printSelectedCells() {
        logger.info("Zeige Debug-Informationen für {} markierte Zellen", selectedCells.size());

        System.out.println("Markierte Zellen:");
        for (Point cell : selectedCells) {
            int viewRow = cell.x;
            int modelRow = table.convertRowIndexToModel(viewRow);
            int col = cell.y;
            String message = String.format("Zeile (View): %d, Zeile (Model): %d, Spalte: %d",
                    viewRow, modelRow, col);
            System.out.println(message);
            logger.debug(message);
        }
    }
}