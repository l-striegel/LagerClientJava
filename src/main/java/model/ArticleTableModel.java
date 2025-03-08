package model;

import javax.swing.table.AbstractTableModel;
import java.util.*;

/**
 * Datenmodell für die Artikel-Tabelle.
 * Verwaltet die Anzeige von Artikeln in einer JTable und die Zuordnung zwischen
 * angezeigten IDs und tatsächlichen API-IDs.
 */
public class ArticleTableModel extends AbstractTableModel {
    private final List<Article> articles;
    private final Set<Article> changedArticles;

    // Maps für die ID-Zuordnung
    private final Map<Integer, Integer> displayIdToApiIdMap = new HashMap<>();
    private final Map<Integer, Integer> apiIdToDisplayIdMap = new HashMap<>();

    // Spaltennamen der Tabelle
    private final String[] columnNames = {
            "ID", "Name", "Typ", "Bestand", "Einheit", "Preis", "Lagerplatz", "Status", "Link"
    };

    // Spaltentypen für Typ-spezifische Verarbeitung
    private final Class<?>[] columnTypes = {
            Integer.class, String.class, String.class, Integer.class,
            String.class, Double.class, String.class, String.class, String.class
    };

    /**
     * Konstruktor mit Artikelliste und Set für geänderte Artikel.
     *
     * @param articles Liste der anzuzeigenden Artikel
     * @param changedArticles Set für die Verfolgung geänderter Artikel
     */
    public ArticleTableModel(List<Article> articles, Set<Article> changedArticles) {
        this.articles = articles;
        this.changedArticles = changedArticles;

        // Initialisiere die ID-Zuordnung
        initializeIdMapping();
    }

    /**
     * Initialisiert die Zuordnung zwischen Display-IDs und API-IDs.
     * Display-IDs sind fortlaufende Nummern (1, 2, 3, ...), während API-IDs die
     * tatsächlichen IDs aus der Datenbank sind, die Lücken haben können.
     */
    private void initializeIdMapping() {
        displayIdToApiIdMap.clear();
        apiIdToDisplayIdMap.clear();

        // Erstelle fortlaufende Display-IDs (1, 2, 3, ...) für jede API-ID
        for (int i = 0; i < articles.size(); i++) {
            int displayId = i + 1; // Display-IDs beginnen bei 1
            int apiId = articles.get(i).id;

            displayIdToApiIdMap.put(displayId, apiId);
            apiIdToDisplayIdMap.put(apiId, displayId);
        }
    }

    /**
     * Ermittelt die API-ID anhand einer Display-ID.
     *
     * @param displayId Die anzuzeigende ID (1, 2, 3, ...)
     * @return Die entsprechende API-ID oder -1, wenn nicht gefunden
     */
    public int getApiIdFromDisplayId(int displayId) {
        return displayIdToApiIdMap.getOrDefault(displayId, -1);
    }

    /**
     * Ermittelt die Display-ID anhand einer API-ID.
     *
     * @param apiId Die API-ID aus der Datenbank
     * @return Die entsprechende Display-ID oder -1, wenn nicht gefunden
     */
    public int getDisplayIdFromApiId(int apiId) {
        return apiIdToDisplayIdMap.getOrDefault(apiId, -1);
    }

    /**
     * Ermittelt die API-ID anhand eines Modellindex.
     *
     * @param modelIndex Der Index in der Artikelliste
     * @return Die entsprechende API-ID oder -1, wenn der Index ungültig ist
     */
    public int getApiIdFromModelIndex(int modelIndex) {
        if (modelIndex >= 0 && modelIndex < articles.size()) {
            return articles.get(modelIndex).id;
        }
        return -1;
    }

    /**
     * Findet einen Artikel anhand seiner API-ID.
     *
     * @param apiId Die zu suchende API-ID
     * @return Das gefundene Article-Objekt oder null, wenn nicht gefunden
     */
    public Article getArticleByApiId(int apiId) {
        for (Article article : articles) {
            if (article.id == apiId) {
                return article;
            }
        }
        return null;
    }

    /**
     * Ermittelt den Modellindex eines Artikels anhand seiner API-ID.
     *
     * @param apiId Die API-ID des Artikels
     * @return Der entsprechende Modellindex oder -1, wenn nicht gefunden
     */
    public int getModelIndexFromApiId(int apiId) {
        for (int i = 0; i < articles.size(); i++) {
            if (articles.get(i).id == apiId) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Aktualisiert die ID-Zuordnung und benachrichtigt die Ansicht über Änderungen.
     * Sollte aufgerufen werden, wenn Artikel hinzugefügt oder entfernt wurden.
     */
    public void refreshIdMapping() {
        initializeIdMapping();
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return articles.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= articles.size()) {
            return null;
        }

        Article article = articles.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> rowIndex + 1; // Display-ID anstatt API-ID anzeigen
            case 1 -> article.name;
            case 2 -> article.type;
            case 3 -> article.stock;
            case 4 -> article.unit;
            case 5 -> article.price;
            case 6 -> article.location;
            case 7 -> article.status;
            case 8 -> article.link;
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Nur die ID-Spalte (0) ist nicht editierbar
        return columnIndex != 0;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= articles.size() || aValue == null) {
            return;
        }

        Article article = articles.get(rowIndex);
        boolean changed = false;

        try {
            switch (columnIndex) {
                case 1 -> { // Name
                    String newValue = aValue.toString();
                    if (!article.name.equals(newValue)) {
                        article.name = newValue;
                        changed = true;
                    }
                }
                case 2 -> { // Typ
                    String newValue = aValue.toString();
                    if (!article.type.equals(newValue)) {
                        article.type = newValue;
                        changed = true;
                    }
                }
                case 3 -> { // Bestand
                    int newValue = parseIntValue(aValue);
                    if (newValue >= 0 && article.stock != newValue) {
                        article.stock = newValue;
                        changed = true;
                    }
                }
                case 4 -> { // Einheit
                    String newValue = aValue.toString();
                    if (!article.unit.equals(newValue)) {
                        article.unit = newValue;
                        changed = true;
                    }
                }
                case 5 -> { // Preis
                    double newValue = parseDoubleValue(aValue);
                    if (newValue >= 0 && article.price != newValue) {
                        article.price = newValue;
                        changed = true;
                    }
                }
                case 6 -> { // Lagerplatz
                    String newValue = aValue.toString();
                    if (!article.location.equals(newValue)) {
                        article.location = newValue;
                        changed = true;
                    }
                }
                case 7 -> { // Status
                    String newValue = aValue.toString();
                    if (!article.status.equals(newValue)) {
                        article.status = newValue;
                        changed = true;
                    }
                }
                case 8 -> { // Link
                    String newValue = aValue.toString();
                    if (!article.link.equals(newValue)) {
                        article.link = newValue;
                        changed = true;
                    }
                }
            }
        } catch (Exception e) {
            // Fehlerbehandlung für ungültige Eingaben
            System.err.println("Fehler beim Setzen des Werts: " + e.getMessage());
        }

        if (changed) {
            changedArticles.add(article);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    /**
     * Versucht, einen String in einen Integer zu konvertieren.
     *
     * @param value Der zu konvertierende Wert
     * @return Der konvertierte Wert oder 0 bei Fehler
     */
    private int parseIntValue(Object value) {
        try {
            if (value instanceof Integer) {
                return (Integer) value;
            }
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Versucht, einen String in einen Double zu konvertieren.
     *
     * @param value Der zu konvertierende Wert
     * @return Der konvertierte Wert oder 0.0 bei Fehler
     */
    private double parseDoubleValue(Object value) {
        try {
            if (value instanceof Double) {
                return (Double) value;
            }
            String valueStr = value.toString().trim().replace(',', '.');
            return Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Gibt alle geänderten Artikel zurück.
     *
     * @return Set mit allen geänderten Artikeln
     */
    public Set<Article> getChangedArticles() {
        return changedArticles;
    }

    /**
     * Löscht alle geänderten Artikel aus dem Tracking.
     */
    public void clearChangedArticles() {
        changedArticles.clear();
    }
}