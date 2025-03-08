package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Repräsentiert die Formatierungseigenschaften einer Zelle in der Tabelle.
 * Wird für die visuelle Darstellung von Zellen in der Benutzeroberfläche verwendet.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CellStyle {
    @JsonProperty("bold")
    public boolean bold;

    @JsonProperty("italic")
    public boolean italic;

    @JsonProperty("underline")
    public boolean underline;

    @JsonProperty("color")
    public String color;

    /**
     * Standard-Konstruktor, der einen Standardstil mit folgenden Eigenschaften erstellt:
     * - kein Fettdruck
     * - keine Kursivschrift
     * - keine Unterstreichung
     * - schwarze Textfarbe
     */
    public CellStyle() {
        this.bold = false;
        this.italic = false;
        this.underline = false;
        this.color = "#000000";
    }

    /**
     * Vollständiger Konstruktor mit allen Formatierungsoptionen.
     *
     * @param bold Ob der Text fett dargestellt werden soll
     * @param italic Ob der Text kursiv dargestellt werden soll
     * @param underline Ob der Text unterstrichen dargestellt werden soll
     * @param color Die Textfarbe als Hex-String (z.B. "#FF0000" für Rot)
     */
    public CellStyle(boolean bold, boolean italic, boolean underline, String color) {
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.color = color != null ? color : "#000000";
    }

    /**
     * Kopier-Konstruktor zum Erstellen einer neuen Instanz mit den gleichen Eigenschaften.
     *
     * @param other Der zu kopierende Stil
     */
    public CellStyle(CellStyle other) {
        this.bold = other.bold;
        this.italic = other.italic;
        this.underline = other.underline;
        this.color = other.color;
    }

    /**
     * Stellt sicher, dass ein gültiger Farbwert vorhanden ist.
     *
     * @return Der aktuelle Farbwert oder Schwarz, wenn kein gültiger Wert vorhanden ist
     */
    public String getValidColor() {
        if (color == null || color.isEmpty()) {
            return "#000000";
        }
        return color;
    }
}