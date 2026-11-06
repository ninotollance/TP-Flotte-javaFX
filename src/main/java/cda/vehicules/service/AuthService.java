package cda.vehicules.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import cda.vehicules.modele.Utilisateur;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;

public class AuthService {
    private static final Logger log = LogManager.getLogger(AuthService.class);
    private static final int MAX_TENTATIVES = 3;
    private static final String FICHIER_JSON = "user.json";
    private final Utilisateur utilisateur;
    private int nbEchecs;
    private boolean connecte;

    public AuthService() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.utilisateur = mapper.readValue(new File(FICHIER_JSON), Utilisateur.class);
        } catch (IOException e) {
            log.warn("Echec : authentification refusée");
            throw new IllegalStateException("Login ou mot de passe incorrect", e);
        }
    }

    public boolean authentifier(String login, String motDePasse) {
        if (utilisateur.getLogin().equals(login) && BCrypt.checkpw(motDePasse, utilisateur.getMotDePasseHash())) {
            log.info("Succès : authentification de {} reussie ", login);
            nbEchecs = 0;
            connecte =true;
            return true;
        } else {
            log.info("Échec : authentification de {} refusée", login);
            nbEchecs++;
            if (estBloque()) {
                log.warn("Echec : limite de tentative de connexion atteinte");

            }
            return false;
        }

    }

    public int  tentativesRestantes() {
        return MAX_TENTATIVES - nbEchecs;
    }

    public boolean estBloque() {
        return nbEchecs >= MAX_TENTATIVES;
    }

    public boolean estConnecte() {
        return connecte;
    }

    public int getNbEchecs() {
        return nbEchecs;
    }

    public void setNbEchecs(int nbEchecs) {
        this.nbEchecs = nbEchecs;
    }
}

