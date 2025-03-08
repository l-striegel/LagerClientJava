package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Zentrale Konfigurationsklasse, die Einstellungen aus config.properties lädt und bereitstellt.
 * Implementiert als Singleton für einfachen Zugriff aus allen Teilen der Anwendung.
 */
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static AppConfig instance;
    private final Properties properties = new Properties();

    /**
     * Privater Konstruktor, der die Konfiguration lädt.
     * Sucht zuerst nach einer externen Konfigurationsdatei, dann nach einer eingebetteten.
     */
    private AppConfig() {
        // Versuche zuerst, eine externe Konfigurationsdatei zu laden
        boolean configLoaded = false;
        try {
            File externalConfig = new File("config.properties");
            if (externalConfig.exists()) {
                try (FileInputStream fis = new FileInputStream(externalConfig)) {
                    properties.load(fis);
                    logger.info("Externe Konfigurationsdatei geladen: {}", externalConfig.getAbsolutePath());
                    configLoaded = true;
                }
            }
        } catch (Exception e) {
            logger.warn("Externe config.properties nicht gefunden oder ungültig: {}", e.getMessage());
        }

        // Fallback auf eingebettete Konfiguration, wenn keine externe gefunden wurde
        if (!configLoaded) {
            try (InputStream is = getClass().getResourceAsStream("/config.properties")) {
                if (is != null) {
                    properties.load(is);
                    logger.info("Interne Konfigurationsdatei geladen");
                } else {
                    logger.warn("Keine interne Konfigurationsdatei gefunden");
                }
            } catch (Exception e) {
                logger.warn("Fehler beim Laden der internen Konfiguration: {}", e.getMessage());
            }
        }

        // Debug-Ausgabe aller geladenen Properties
        if (logger.isDebugEnabled()) {
            logger.debug("Geladene Konfiguration:");
            for (String key : properties.stringPropertyNames()) {
                logger.debug("  {} = {}", key, properties.getProperty(key));
            }
        }
    }

    /**
     * Gibt die Singleton-Instanz zurück.
     * Bei erstem Aufruf wird die Konfiguration geladen.
     *
     * @return Die AppConfig-Instanz
     */
    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    /**
     * Gibt einen String-Wert aus der Konfiguration zurück.
     *
     * @param key Der Schlüssel der gewünschten Eigenschaft
     * @param defaultValue Der Standardwert, falls die Eigenschaft nicht existiert
     * @return Der Wert der Eigenschaft oder der Standardwert
     */
    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gibt einen Integer-Wert aus der Konfiguration zurück.
     *
     * @param key Der Schlüssel der gewünschten Eigenschaft
     * @param defaultValue Der Standardwert, falls die Eigenschaft nicht existiert oder ungültig ist
     * @return Der Wert der Eigenschaft oder der Standardwert
     */
    public int getInt(String key, int defaultValue) {
        try {
            String value = properties.getProperty(key);
            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            logger.warn("Ungültiger Integer-Wert für '{}': {}", key, properties.getProperty(key));
        }
        return defaultValue;
    }

    /**
     * Gibt einen Boolean-Wert aus der Konfiguration zurück.
     *
     * @param key Der Schlüssel der gewünschten Eigenschaft
     * @param defaultValue Der Standardwert, falls die Eigenschaft nicht existiert
     * @return Der Wert der Eigenschaft oder der Standardwert
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    /**
     * Gibt die API-URL zurück.
     *
     * @return Die konfigurierte API-URL oder die Standard-URL
     */
    public String getApiUrl() {
        return getString("api.url", "https://localhost:5001/api/article");
    }

    /**
     * Gibt zurück, ob der Debug-Modus aktiviert ist.
     *
     * @return true wenn Debug-Modus aktiviert ist, sonst false
     */
    public boolean isDebugMode() {
        return getBoolean("app.debug", false);
    }

    /**
     * Gibt die konfigurierte Tabellenzeilenhöhe zurück.
     *
     * @return Die Zeilenhöhe oder 25 als Standardwert
     */
    public int getTableRowHeight() {
        return getInt("ui.table.rowheight", 25);
    }

    /**
     * Gibt die Farbe für Zebrastreifen zurück.
     *
     * @return Die Farbe als Hex-String oder #F0F0F0 als Standardwert
     */
    public String getZebraStripeColor() {
        return getString("ui.table.zebracolor", "#F0F0F0");
    }
}