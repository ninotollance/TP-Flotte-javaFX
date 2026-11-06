package cda.vehicules.modele;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Vehicule {

    private static final String FORMAT_IMMAT = "[A-Z]{2}-\\d{3}-[A-Z]{2}";
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    private int id;
    private String immatriculation;
    private String marque;
    private String modele;
    private LocalDate dateMiseCirculation;
    private int kilometrage;

    public Vehicule(int id, String immatriculation, String marque, String modele, LocalDate dateMiseCirculation, int kilometrage) {
        this.id = id;
        setImmatriculation(immatriculation);
        this.marque = marque;
        this.modele = modele;
        this.dateMiseCirculation = dateMiseCirculation;
        setKilometrage(kilometrage);
    }

    public Vehicule(String immatriculation, String marque, String modele, LocalDate dateMiseCirculation, int kilometrage) {
        setImmatriculation(immatriculation);
        this.marque = marque;
        this.modele = modele;
        this.dateMiseCirculation = dateMiseCirculation;
        setKilometrage(kilometrage);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        if (immatriculation == null) {
            throw new IllegalArgumentException("l'immatriculation est oobligatoire");
        }
        String immat = immatriculation.trim().toUpperCase();
        if(!immat.matches(FORMAT_IMMAT)) {
            throw new IllegalArgumentException("Immatriculation invalide : " + immatriculation + " (ex:  AA-123-AA).");
        }
        this.immatriculation = immat;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public LocalDate getdateMiseCirculation() {
        return dateMiseCirculation;
    }

    public void setdateMiseCirculation(LocalDate dateMiseCirculation) {
        this.dateMiseCirculation = dateMiseCirculation;
    }

    public int getKilometrage() {
        return kilometrage;
    }

    public void setKilometrage(int kilometrage) {
        if (kilometrage < 0) {
            throw new IllegalArgumentException("Le kilométrage doit être positif.");
        }
        this.kilometrage = kilometrage;
    }

    @Override
    public String toString() {
        return String.format("[%03d]  %s | %s %s | mise en circulation le %s | %d km",
                id, immatriculation, marque, modele,
                FORMAT_DATE.format(dateMiseCirculation), kilometrage);
    }
}

