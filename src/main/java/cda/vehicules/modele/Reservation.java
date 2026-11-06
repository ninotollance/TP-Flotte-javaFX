package cda.vehicules.modele;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class Reservation {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");


    private Vehicule vehicule;
    private Timestamp date_depart;
    private Timestamp date_retour;
    private int km_retour;

    public Reservation(Vehicule vehicule, Timestamp date_depart, Timestamp date_retour, int km_retour) {
        this.vehicule = vehicule;
        this.date_depart = date_depart;
        this.date_retour = date_retour;
        this.km_retour = km_retour;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    public Timestamp getDate_depart() {
        return date_depart;
    }

    public void setDate_depart(Timestamp date_depart) {
        this.date_depart = date_depart;
    }

    public Timestamp getDate_retour() {
        return date_retour;
    }

    public void setDate_retour(Timestamp date_retour) {
        this.date_retour = date_retour;
    }

    public int getKm_retour() {
        return km_retour;
    }

    public void setKm_retour(int km_retour) {
        this.km_retour = km_retour;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(vehicule.getImmatriculation())
                .append(" | véhicule réservé le ")
                .append(FORMAT_DATE.format(date_depart.toLocalDateTime()));

        if (date_retour != null) {
            sb.append(", rendu le ")
                    .append(FORMAT_DATE.format(date_retour.toLocalDateTime()))
                    .append(" | ")
                    .append(km_retour)
                    .append(" km parcourus");
        }
        return sb.toString();
    }
}
