package cda.vehicules.modele;

import java.time.LocalDate;

public class Salarie {

    private static final String FORMAT_MATRICULE = "\\d{6}";

    private String nom;
    private String prenom;
    private String matricule;

    public Salarie(String nom, String prenom, String matricule) {
        this.nom = nom;
        this.prenom = prenom;
        setMatricule(matricule);
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        if(matricule == null || !matricule.trim().matches(FORMAT_MATRICULE)) {
            throw new IllegalArgumentException("Matricule invalide : " + matricule + " ex: 256258.");
        }
        this.matricule = matricule.trim();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %s", matricule, prenom, nom);
    }
}
