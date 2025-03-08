package ui;

import model.Article;
import model.CellStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Lauscher für Mausevents auf der Tabelle.
 * Ermöglicht kontextsensitive Menüs für die Formatierung von Zellen.
 */
public class TableMouseListener extends MouseAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TableMouseListener.class);

    private final JTable table;
    private final List<Article> articles;
    private final Set<Article> changedArticles;

    /**
     * Erstellt einen neuen TableMouseListener.
     *
     * @param table Die JTable, auf die der Listener angewendet wird
     * @param articles Die Liste der Artikel
     * @param changedArticles Das Set zum Verfolgen geänderter Artikel
     */
    public TableMouseListener(JTable table, List<Article> articles, Set<Article> changedArticles) {
        this.table = table;
        this.articles = articles;
        this.changedArticles = changedArticles;
        logger.debug("TableMouseListener initialisiert für Tabelle mit {} Artikeln", articles.size());
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if (event.isPopupTrigger()) {
            logger.debug("Popup-Trigger durch mousePressed an Position {},{}",
                    event.getX(), event.getY());
            showPopup(event);
        }
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        if (event.isPopupTrigger()) {
            logger.debug("Popup-Trigger durch mouseReleased an Position {},{}",
                    event.getX(), event.getY());
            showPopup(event);
        }
    }

    /**
     * Zeigt ein Kontextmenü für die Formatierung der Zelle an.
     *
     * @param event Das MouseEvent, das das Popup ausgelöst hat
     */
    private void showPopup(MouseEvent event) {
        // Bestimme die Zeile und Spalte an der Mausposition
        int viewRow = table.rowAtPoint(event.getPoint());
        int modelRow = table.convertRowIndexToModel(viewRow);
        int col = table.columnAtPoint(event.getPoint());

        logger.debug("Kontextmenü angefordert für Zeile {} (Modell: {}), Spalte {}",
                viewRow, modelRow, col);

        // Prüfe, ob die Position gültig ist
        if (modelRow == -1 || col == -1 || modelRow >= articles.size()) {
            logger.warn("Ungültige Zellenposition für Kontextmenü: {}x{}", viewRow, col);
            return;
        }

        String columnName = table.getColumnName(col);
        Article article = articles.get(modelRow);

        // Keine Formatierung für die ID-Spalte
        if (columnName.equals("ID")) {
            logger.debug("Kontextmenü für ID-Spalte nicht erlaubt");
            return;
        }

        logger.debug("Zeige Kontextmenü für Artikel ID {} in Spalte '{}'", article.id, columnName);

        // Stelle sicher, dass ein styles-Objekt und ein CellStyle-Objekt existieren
        ensureStyleExists(article, columnName);

        CellStyle style = article.styles.get(columnName);

        // Erstelle das Popup-Menü
        JPopupMenu popupMenu = createFormatMenu(article, style, columnName);
        popupMenu.show(table, event.getX(), event.getY());
    }

    /**
     * Stellt sicher, dass ein CellStyle-Objekt für die angegebene Spalte existiert.
     *
     * @param article Der Artikel
     * @param columnName Der Name der Spalte
     */
    private void ensureStyleExists(Article article, String columnName) {
        if (article.styles == null) {
            logger.trace("Styles-Map für Artikel ID {} erstellt", article.id);
            article.styles = new HashMap<>();
        }

        if (!article.styles.containsKey(columnName)) {
            logger.trace("CellStyle für Artikel ID {} in Spalte '{}' erstellt", article.id, columnName);
            article.styles.put(columnName, new CellStyle());
        }
    }

    /**
     * Erstellt ein Formatierungsmenü für die Zelle.
     *
     * @param article Der Artikel
     * @param style Der Zellenstil
     * @param columnName Der Spaltenname für Log-Ausgaben
     * @return Das erstellte JPopupMenu
     */
    private JPopupMenu createFormatMenu(Article article, CellStyle style, String columnName) {
        logger.debug("Erstelle Formatierungsmenü für Artikel ID {} in Spalte '{}'", article.id, columnName);

        JPopupMenu popupMenu = new JPopupMenu();

        // Fett-Option
        JCheckBoxMenuItem boldItem = new JCheckBoxMenuItem("Fett", style.bold);
        boldItem.addActionListener(e -> {
            style.bold = !style.bold;
            logger.debug("Fett-Status für Artikel ID {} in Spalte '{}' geändert auf: {}",
                    article.id, columnName, style.bold);
            changedArticles.add(article);
            table.repaint();
        });

        // Kursiv-Option
        JCheckBoxMenuItem italicItem = new JCheckBoxMenuItem("Kursiv", style.italic);
        italicItem.addActionListener(e -> {
            style.italic = !style.italic;
            logger.debug("Kursiv-Status für Artikel ID {} in Spalte '{}' geändert auf: {}",
                    article.id, columnName, style.italic);
            changedArticles.add(article);
            table.repaint();
        });

        // Farb-Option
        JMenuItem colorItem = new JMenuItem("Farbe ändern...");
        colorItem.addActionListener(e -> {
            Color initialColor = Color.BLACK;
            try {
                if (style.color != null && !style.color.isEmpty()) {
                    initialColor = Color.decode(style.color);
                    logger.trace("Initialfarbe für Farbwähler: {}", style.color);
                }
            } catch (Exception ex) {
                logger.warn("Fehler beim Parsen der initialen Farbe: {}", ex.getMessage());
            }

            logger.debug("Öffne Farbwähler für Artikel ID {} in Spalte '{}'", article.id, columnName);
            Color newColor = JColorChooser.showDialog(
                    table,
                    "Wähle eine Farbe",
                    initialColor
            );

            if (newColor != null) {
                String hexColor = String.format("#%02x%02x%02x",
                        newColor.getRed(), newColor.getGreen(), newColor.getBlue());
                style.color = hexColor;

                logger.debug("Neue Farbe für Artikel ID {} in Spalte '{}' gesetzt: {}",
                        article.id, columnName, hexColor);

                changedArticles.add(article);
                table.repaint();
            } else {
                logger.debug("Farbauswahl abgebrochen");
            }
        });

        // Füge Menüpunkte hinzu
        popupMenu.add(boldItem);
        popupMenu.add(italicItem);
        popupMenu.add(new JSeparator());
        popupMenu.add(colorItem);

        logger.trace("Kontextmenü mit {} Einträgen erstellt", popupMenu.getComponentCount());
        return popupMenu;
    }
}