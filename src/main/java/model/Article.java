package model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Repräsentiert einen Artikel im Lagerverwaltungssystem.
 * Diese Klasse enthält alle Eigenschaften eines Artikels sowie Formatierungsinformationen für die Anzeige.
 */
public class Article {
    @JsonProperty("id")
    public int id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("type")
    public String type;

    @JsonProperty("stock")
    public int stock;

    @JsonProperty("unit")
    public String unit;

    @JsonProperty("price")
    public double price;

    @JsonProperty("location")
    public String location;

    @JsonProperty("status")
    public String status;

    @JsonProperty("link")
    public String link;

    @JsonProperty("timestamp")
    public String timestamp;

    @JsonProperty("stylesJson")
    public String stylesJson;

    @JsonProperty("styles")
    public Map<String, CellStyle> styles;

    /**
     * Standard-Konstruktor für Jackson.
     * Initialisiert ein leeres styles-Map-Objekt.
     */
    public Article() {
        this.styles = new HashMap<>();
    }

    /**
     * Haupt-Konstruktor mit allen Feldern.
     *
     * @param id Die eindeutige ID des Artikels
     * @param name Der Name des Artikels
     * @param type Der Typ des Artikels
     * @param stock Der aktuelle Bestand
     * @param unit Die Einheit (z.B. "Stück", "Liter")
     * @param price Der Preis pro Einheit
     * @param location Der Lagerort
     * @param status Der Status des Artikels (z.B. "Auf Lager")
     * @param link Ein externer Link zum Artikel
     * @param timestamp Der Zeitstempel der letzten Änderung
     * @param stylesJson Die Formatierungsoptionen als JSON-String
     * @param styles Map mit Formatierungsoptionen für verschiedene Spalten
     */
    public Article(int id, String name, String type, int stock, String unit, double price,
                   String location, String status, String link, String timestamp,
                   String stylesJson, Map<String, CellStyle> styles) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.stock = stock;
        this.unit = unit;
        this.price = price;
        this.location = location;
        this.status = status;
        this.link = link;
        this.timestamp = timestamp;
        this.stylesJson = stylesJson;
        this.styles = styles != null ? new HashMap<>(styles) : new HashMap<>();
    }

    /**
     * Kopier-Konstruktor für die Erstellung einer tiefen Kopie eines Artikels.
     *
     * @param article Der zu kopierende Artikel
     */
    public Article(Article article) {
        this(article.id, article.name, article.type, article.stock, article.unit, article.price,
                article.location, article.status, article.link, article.timestamp,
                article.stylesJson, article.styles);
    }

    /**
     * Überprüft, ob ein Artikel alle erforderlichen Felder hat.
     *
     * @return true wenn alle erforderlichen Felder ausgefüllt sind, sonst false
     */
    public boolean isValid() {
        return name != null && !name.isEmpty() &&
                type != null && !type.isEmpty() &&
                stock >= 0 &&
                unit != null && !unit.isEmpty();
    }

    /**
     * Erzeugt eine String-Repräsentation des Artikels.
     *
     * @return Eine formatierte String-Darstellung des Artikels
     */
    @Override
    public String toString() {
        return String.format("#%d: %s (%s) - Bestand: %d %s, Preis: %.2f€",
                id, name, type, stock, unit, price);
    }
}