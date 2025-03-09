package model;

/**
 * Repräsentiert Unterschiede zwischen lokaler und Server-Version eines Artikels.
 */
public class ArticleDifference {
    private Article localArticle;
    private Article serverArticle;
    private boolean isNewLocalArticle;

    /**
     * Konstruktor für vorhandene Artikel.
     *
     * @param local Der lokale Artikel
     * @param server Der Server-Artikel oder null, wenn auf dem Server gelöscht
     */
    public ArticleDifference(Article local, Article server) {
        this(local, server, false);
    }

    /**
     * Vollständiger Konstruktor.
     *
     * @param local Der lokale Artikel
     * @param server Der Server-Artikel oder null, wenn auf dem Server gelöscht oder neu
     * @param isNew Ob es sich um einen neu hinzugefügten lokalen Artikel handelt
     */
    public ArticleDifference(Article local, Article server, boolean isNew) {
        this.localArticle = local;
        this.serverArticle = server;
        this.isNewLocalArticle = isNew;
    }

    /**
     * Prüft, ob der Artikel auf dem Server gelöscht wurde.
     *
     * @return true wenn der Artikel auf dem Server nicht mehr existiert
     */
    public boolean isDeleted() {
        return serverArticle == null && !isNewLocalArticle;
    }

    /**
     * Gibt Zugriff auf den lokalen Artikel.
     *
     * @return Der lokale Artikel
     */
    public Article getLocalArticle() {
        return localArticle;
    }

    /**
     * Gibt Zugriff auf den Server-Artikel.
     *
     * @return Der Server-Artikel oder null
     */
    public Article getServerArticle() {
        return serverArticle;
    }

    /**
     * Gibt zurück, ob der Artikel lokal neu ist.
     *
     * @return true wenn der Artikel nur lokal existiert und hochgeladen werden soll
     */
    public boolean isNewLocalArticle() {
        return isNewLocalArticle;
    }

    /**
     * Erstellt eine Beschreibung der Unterschiede.
     *
     * @return Eine formatierte Beschreibung der Unterschiede
     */
    public String getDifferenceDescription() {
        if (isNewLocalArticle) {
            return "Neu hinzugefügter Artikel (lokal): " + localArticle.name;
        } else if (isDeleted()) {
            return "Artikel auf Server gelöscht: " + localArticle.name;
        } else {
            StringBuilder diff = new StringBuilder("Änderungen für Artikel #" +
                    localArticle.id + " (" + localArticle.name + "):\n");

            // Felder vergleichen
            if (!localArticle.name.equals(serverArticle.name)) {
                diff.append("  - Name: ").append(serverArticle.name).append(" -> ").append(localArticle.name).append("\n");
            }
            if (!localArticle.type.equals(serverArticle.type)) {
                diff.append("  - Typ: ").append(serverArticle.type).append(" -> ").append(localArticle.type).append("\n");
            }
            if (localArticle.stock != serverArticle.stock) {
                diff.append("  - Bestand: ").append(serverArticle.stock).append(" -> ").append(localArticle.stock).append("\n");
            }
            if (!localArticle.unit.equals(serverArticle.unit)) {
                diff.append("  - Einheit: ").append(serverArticle.unit).append(" -> ").append(localArticle.unit).append("\n");
            }
            if (localArticle.price != serverArticle.price) {
                diff.append("  - Preis: ").append(serverArticle.price).append(" -> ").append(localArticle.price).append("\n");
            }
            if (!localArticle.location.equals(serverArticle.location)) {
                diff.append("  - Lagerplatz: ").append(serverArticle.location).append(" -> ").append(localArticle.location).append("\n");
            }
            if (!localArticle.status.equals(serverArticle.status)) {
                diff.append("  - Status: ").append(serverArticle.status).append(" -> ").append(localArticle.status).append("\n");
            }

            return diff.toString();
        }
    }
}