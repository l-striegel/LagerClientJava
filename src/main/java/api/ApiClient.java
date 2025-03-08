package api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Client für die Kommunikation mit der Artikel-API.
 * Stellt Methoden zum Abrufen, Erstellen, Aktualisieren und Löschen von Artikeln bereit.
 */
public class ApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private static String API_BASE_URL = "https://localhost:5001/api/article";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Konfiguriere ObjectMapper für JSON-Deserialisierung
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        logger.debug("ObjectMapper konfiguriert mit FAIL_ON_UNKNOWN_PROPERTIES=false");

        // Versuche zuerst, eine externe Konfigurationsdatei zu laden
        boolean configLoaded = false;
        try {
            File externalConfig = new File("config.properties");
            if (externalConfig.exists()) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(externalConfig)) {
                    props.load(fis);
                    String configUrl = props.getProperty("api.url");
                    if (configUrl != null && !configUrl.isEmpty()) {
                        logger.info("API-URL aus externer Konfiguration geladen: {} (war: {})", configUrl, API_BASE_URL);
                        API_BASE_URL = configUrl;
                        configLoaded = true;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Externe config.properties nicht gefunden oder ungültig: {}.", e.getMessage());
        }

        // Fallback auf eingebettete Konfiguration, wenn keine externe gefunden wurde
        if (!configLoaded) {
            try {
                Properties props = new Properties();
                InputStream inStream = ApiClient.class.getResourceAsStream("/config.properties");
                if (inStream != null) {
                    props.load(inStream);
                    String configUrl = props.getProperty("api.url");
                    if (configUrl != null && !configUrl.isEmpty()) {
                        logger.info("API-URL aus interner Konfiguration geladen: {} (war: {})", configUrl, API_BASE_URL);
                        API_BASE_URL = configUrl;
                    } else {
                        logger.info("Keine API-URL in Konfiguration gefunden, verwende Standard-URL: {}", API_BASE_URL);
                    }
                } else {
                    logger.warn("Interne config.properties nicht gefunden. Verwende Standard-URL: {}", API_BASE_URL);
                }
            } catch (Exception e) {
                logger.warn("Interne config.properties nicht gefunden oder ungültig: {}. Verwende Standard-URL: {}",
                        e.getMessage(), API_BASE_URL);
            }
        }

        // Deaktiviere SSL-Zertifikatsprüfung für Entwicklungsumgebungen
        disableSSLCertificateChecking();
    }

    /**
     * Ruft alle Artikel von der API ab.
     *
     * @return Liste aller Artikel
     */
    public static List<Article> fetchArticles() {
        logger.info("Rufe alle Artikel von der API ab");
        try {
            String responseJson = sendGetRequest(API_BASE_URL);

            // Bei sehr detailliertem Logging (TRACE) die vollständige Antwort loggen
            logger.trace("API-Antwort: {}", responseJson);

            // Bei normalem Debug-Level nur die Größe der Antwort loggen
            logger.debug("API-Antwort erhalten: {} Zeichen", responseJson.length());

            List<Article> articles = Arrays.asList(objectMapper.readValue(responseJson, Article[].class));
            logger.info("{} Artikel erfolgreich abgerufen", articles.size());

            return articles;
        } catch (Exception e) {
            logger.error("Fehler beim Abrufen der Artikel: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Ruft einen einzelnen Artikel anhand seiner ID ab.
     *
     * @param id Die ID des abzurufenden Artikels
     * @return Das Article-Objekt oder null bei Fehler
     * @throws Exception Wenn der Artikel nicht abgerufen werden kann
     */
    public static Article fetchArticle(int id) throws Exception {
        logger.info("Rufe Artikel mit ID {} ab", id);
        try {
            String url = API_BASE_URL + "/" + id;
            String responseJson = sendGetRequest(url);

            logger.trace("Antwort für Artikel {}: {}", id, responseJson);

            Article article = objectMapper.readValue(responseJson, Article.class);
            logger.info("Artikel mit ID {} erfolgreich abgerufen: {}", id, article.name);
            return article;
        } catch (Exception e) {
            logger.error("Fehler beim Abrufen des Artikels mit ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Hilfsmethode für GET-Requests.
     *
     * @param url Die URL für den GET-Request
     * @return Die Antwort als String
     * @throws Exception Bei Netzwerk- oder Serverproblemen
     */
    private static String sendGetRequest(String url) throws Exception {
        logger.debug("Sende GET-Request an: {}", url);
        HttpURLConnection conn = null;
        try {
            conn = createConnection(url, "GET");

            int responseCode = conn.getResponseCode();
            logger.debug("GET-Response-Code: {}", responseCode);

            if (responseCode != 200) {
                logger.warn("Unerwarteter HTTP-Response-Code: {}", responseCode);
                throw new RuntimeException("HTTP-Fehler: " + responseCode);
            }

            String response = readResponse(conn);
            logger.debug("GET-Request erfolgreich: {} Zeichen empfangen", response.length());
            return response;
        } catch (Exception e) {
            logger.error("Fehler bei GET-Request an {}: {}", url, e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                conn.disconnect();
                logger.trace("Verbindung geschlossen");
            }
        }
    }

    /**
     * Erstellt eine HTTP-Verbindung mit den angegebenen Parametern.
     *
     * @param urlString Die URL für die Verbindung
     * @param method Die HTTP-Methode (GET, POST, PUT, DELETE)
     * @return Die konfigurierte HttpURLConnection
     * @throws Exception Bei Problemen mit der Verbindung
     */
    private static HttpURLConnection createConnection(String urlString, String method) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Accept", "application/json");
        if (method.equals("POST") || method.equals("PUT")) {
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
        }
        logger.trace("Verbindung erstellt: {} {}", method, urlString);
        return conn;
    }

    /**
     * Liest die Antwort aus einer HTTP-Verbindung.
     *
     * @param conn Die HttpURLConnection
     * @return Die Antwort als String
     * @throws Exception Bei Problemen beim Lesen der Antwort
     */
    private static String readResponse(HttpURLConnection conn) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (Exception e) {
            logger.error("Fehler beim Lesen der HTTP-Antwort: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Deaktiviert die SSL-Zertifikatsprüfung für Entwicklungszwecke.
     * HINWEIS: Diese Methode sollte in Produktionsumgebungen nicht verwendet werden!
     */
    private static void disableSSLCertificateChecking() {
        try {
            logger.warn("SSL-Zertifikatsprüfung wird deaktiviert. NICHT FÜR PRODUKTION GEEIGNET!");
            TrustManager[] trustAllCertificates = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCertificates, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            logger.info("SSL-Zertifikatsprüfung wurde deaktiviert");
        } catch (Exception e) {
            logger.error("Fehler beim Deaktivieren der SSL-Zertifikatsprüfung: {}", e.getMessage(), e);
        }
    }
}