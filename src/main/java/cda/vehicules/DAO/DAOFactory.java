package cda.vehicules.DAO;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DAOFactory {

    private static final String FICHIER_PROPERTIES = "dao.properties";

    private static DAOFactory instance;

    private final String url;
    private final String user;
    private final String password;

    private Connection connexion;

    private DAOFactory(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static synchronized DAOFactory getInstance() {
        if (instance == null) {
            Properties props = new Properties();
            try (InputStream in = DAOFactory.class.getClassLoader().getResourceAsStream(FICHIER_PROPERTIES)) {
                if (in == null) {
                    throw new IllegalStateException("Fichier de configuration introuvable : " + FICHIER_PROPERTIES);
                }
                props.load(in);
            } catch (IOException e) {
                throw new UncheckedIOException("Lecture impossible de " + FICHIER_PROPERTIES, e);
            }

            String url = props.getProperty("url");
            String user = props.getProperty("user");
            if (url == null || user == null) {
                throw new IllegalStateException("Les propriétés 'url' et 'user' sont obligatoires dans " + FICHIER_PROPERTIES);
            }
            String password = props.getProperty("password", "");

            instance = new DAOFactory(url.trim(), user.trim(), password);
        }
        return instance;
    }

    public synchronized Connection getConnexion() throws SQLException {
        if (connexion == null || connexion.isClosed() || !connexion.isValid(2)) {
            connexion = DriverManager.getConnection(url, user, password);
        }
        return connexion;
    }

    public synchronized void fermerConnexion() {
        if (connexion != null) {
            try {
                connexion.close();
            } catch (SQLException e) {
                System.err.println("Erreur à la fermeture de la connexion : " + e.getMessage());
            }
            connexion = null;
        }
    }

    // Ajouter les getDAO
}