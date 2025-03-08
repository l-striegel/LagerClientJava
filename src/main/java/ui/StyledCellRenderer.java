package ui;

import config.AppConfig;
import model.Article;
import model.CellStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Properties;

/**
 * Benutzerdefinierter Renderer für Tabellenzellen, der Formatierungen basierend auf CellStyle anwendet.
 * Unterstützt Fett, Kursiv und Textfarben sowie die visuelle Markierung ausgewählter Zellen.
 */
public class StyledCellRenderer extends DefaultTableCellRenderer {
    private static final Logger logger = LoggerFactory.getLogger(StyledCellRenderer.class);

    private final List<Article> articles;
    private final Color selectionColor;
    private final Color zebraStripeColor;

    /**
     * Erstellt einen neuen StyledCellRenderer.
     *
     * @param articles Die Liste der Artikel für Formatierungen
     */
    public StyledCellRenderer(List<Article> articles) {
        logger.debug("Initialisiere StyledCellRenderer mit {} Artikeln", articles.size());
        this.articles = articles;

        // Farben aus der zentralen Konfiguration laden
        AppConfig config = AppConfig.getInstance();

        // Zebrastreifen-Farbe laden
        Color tempZebraColor;
        try {
            String zebraColorStr = config.getZebraStripeColor();
            tempZebraColor = Color.decode(zebraColorStr);
            logger.debug("Zebrastreifen-Farbe aus Konfiguration geladen: {}", zebraColorStr);
        } catch (Exception e) {
            logger.warn("Fehler beim Dekodieren der Zebrastreifen-Farbe: {}", e.getMessage());
            tempZebraColor = new Color(240, 240, 240); // Fallback auf Hellgrau
        }

        // Farben endgültig setzen
        this.selectionColor = new Color(173, 216, 230); // Hellblau
        this.zebraStripeColor = tempZebraColor;
        logger.debug("Renderer initialisiert mit Auswahlfarbe {} und Zebrastreifen-Farbe {}",
                colorToString(selectionColor), colorToString(zebraStripeColor));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Standard-Rendering zuerst durchführen, aber isSelected auf false setzen, um
        // die Standard-Auswahl zu deaktivieren
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, false, hasFocus, row, column);

        // Zebrastreifen für bessere Lesbarkeit
        applyZebraStripeBackground(label, row);

        // Markierte Zellen hervorheben
        highlightSelectedCells(table, label, row, column);

        // Artikel-spezifische Formatierungen anwenden
        applyArticleStyles(table, label, row, column);

        return label;
    }

    /**
     * Wendet Zebrastreifen-Formatierung auf die Zelle an.
     *
     * @param label Die zu formatierende Zelle
     * @param row Die Zeilenposition
     */
    private void applyZebraStripeBackground(JLabel label, int row) {
        if (row % 2 == 0) {
            label.setBackground(zebraStripeColor);
        } else {
            label.setBackground(Color.WHITE);
        }
    }

    /**
     * Hebt ausgewählte Zellen hervor.
     *
     * @param table Die JTable
     * @param label Die zu formatierende Zelle
     * @param row Die Zeilenposition
     * @param column Die Spaltenposition
     */
    private void highlightSelectedCells(JTable table, JLabel label, int row, int column) {
        try {
            // Finde die LagerClientApp-Instanz
            Component comp = table;
            while (comp != null && !(comp instanceof JFrame)) {
                comp = comp.getParent();
            }

            if (comp instanceof JFrame) {
                LagerClientApp app = (LagerClientApp) ((JFrame) comp).getRootPane().getClientProperty("appInstance");
                if (app != null) {
                    Point viewCell = new Point(row, column);
                    if (app.getSelectedCells().contains(viewCell)) {
                        label.setBackground(selectionColor);
                        logger.trace("Zelle {},{} als ausgewählt markiert", row, column);
                    }
                }
            }
        } catch (Exception e) {
            // Ignoriere Fehler beim Finden der Anwendung oder beim Abrufen der markierten Zellen
            logger.warn("Fehler beim Hervorheben markierter Zellen: {}", e.getMessage());
        }
    }

    /**
     * Wendet die Formatierungen des Artikels auf die Zelle an.
     *
     * @param table Die JTable
     * @param label Die zu formatierende Zelle
     * @param row Die Zeilenposition
     * @param column Die Spaltenposition
     */
    private void applyArticleStyles(JTable table, JLabel label, int row, int column) {
        try {
            // Konvertiere View-Zeile zu Modell-Zeile um den korrekten Artikel zu bekommen
            int modelRow = table.convertRowIndexToModel(row);
            if (modelRow >= 0 && modelRow < articles.size()) {
                Article article = articles.get(modelRow);
                String columnName = table.getColumnName(column);

                if (article.styles != null && article.styles.containsKey(columnName)) {
                    CellStyle style = article.styles.get(columnName);
                    logger.trace("Formatierung für Artikel ID {} in Spalte '{}': bold={}, italic={}, color={}",
                            article.id, columnName, style.bold, style.italic, style.color);

                    // Textfarbe anwenden
                    if (style.color != null && !style.color.isEmpty()) {
                        try {
                            label.setForeground(Color.decode(style.color));
                        } catch (Exception e) {
                            logger.warn("Ungültige Farbangabe für Artikel ID {} in Spalte '{}': {}",
                                    article.id, columnName, style.color);
                            label.setForeground(Color.BLACK);
                        }
                    }

                    // Schriftstil anwenden (fett/kursiv)
                    Font currentFont = label.getFont();
                    int fontStyle = Font.PLAIN;
                    if (style.bold) fontStyle |= Font.BOLD;
                    if (style.italic) fontStyle |= Font.ITALIC;
                    if (fontStyle != Font.PLAIN) {
                        label.setFont(new Font(currentFont.getFamily(), fontStyle, currentFont.getSize()));
                    }
                }
            }
        } catch (Exception e) {
            // Ignoriere Fehler beim Anwenden von Stilen
            logger.warn("Fehler beim Anwenden von Artikelstilen: {}", e.getMessage());
        }
    }

    /**
     * Konvertiert eine Farbe in einen lesbaren String.
     *
     * @param color Die zu konvertierende Farbe
     * @return Eine String-Repräsentation der Farbe
     */
    private String colorToString(Color color) {
        return String.format("RGB(%d,%d,%d)", color.getRed(), color.getGreen(), color.getBlue());
    }
}