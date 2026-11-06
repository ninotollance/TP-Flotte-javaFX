package cda.vehicules.controlleur;

import cda.vehicules.exception.RegleGestionException;
import cda.vehicules.modele.Reservation;
import cda.vehicules.modele.Salarie;
import cda.vehicules.modele.Vehicule;
import cda.vehicules.service.ReservationService;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    public String creerSalarie(String nom, String prenom, String matricule) {
        try {
            Salarie s = new Salarie(matricule, nom, prenom);
            reservationService.creerSalarie(s);
            return "Salarié " + matricule + " — " + prenom + " " + nom + " créé.";
        } catch (RegleGestionException | IllegalArgumentException e) {
            return "Refusé : " + e.getMessage();
        }
    }

    public String enregistrerDepart(String matricule, String immatriulation) {
        try {
            reservationService.enregistrerDepart(matricule, immatriulation);
            return "Depart enregistré : " + immatriulation + " " + matricule;
        } catch (RegleGestionException e) {
            return "Départ refusé : " + e.getMessage();
        }
    }


    public String enregistrerRetour(String immatriculation, int kmRetour) {
        try {
            int kmParcourus = reservationService.enregistrerRetour(immatriculation, kmRetour);
            return "Retour enregistré : " + immatriculation + ", " + kmParcourus
                    + " km parcourus. Nouveau kilométrage : " + kmRetour + " km.";
        } catch (RegleGestionException e) {
            return "Retour refusé : " + e.getMessage();
        }
    }

    public String vehiculeEnDepalcement() {
        StringBuilder vehicule = new StringBuilder();
        List<Vehicule> liste = reservationService.vehiculeEnDeplacement();
        vehicule.append("VÉHICULES EN DÉPLACEMENT — ").append(liste.size()).append("\n");
        for (Vehicule v : liste) {
            vehicule.append(v.getImmatriculation()).append(" ").append(v.getMarque()).append(" ").append(v.getModele()).append("\n");
        }
        return vehicule.toString();
    }

    public String historique(String matricule) {
        try {
            StringBuilder historique = new StringBuilder();
            Map<Timestamp, Reservation> resa = reservationService.historique(matricule);
            historique.append("HISTORIQUE — ").append(matricule)
                    .append(" — ").append(resa.size()).append(" réservation(s)\n");
            for (Map.Entry<Timestamp, Reservation> entree : resa.entrySet()) {
                Reservation r = entree.getValue();
                String retour = (r.getDate_retour() == null) ? "en cours" : String.valueOf(r.getDate_retour());
                historique.append(entree.getKey()).append(" → ").append(retour)
                        .append(" ").append(r.getVehicule().getImmatriculation()).append("\n");
            }
            return historique.toString();
        } catch (RegleGestionException e) {
            return ("Refusé : " + e.getMessage());
        }

    }

}
