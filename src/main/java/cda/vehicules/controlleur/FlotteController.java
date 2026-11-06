package cda.vehicules.controlleur;

import cda.vehicules.exception.RegleGestionException;
import cda.vehicules.modele.Flotte;
import cda.vehicules.modele.Vehicule;
import cda.vehicules.service.VehiculeService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FlotteController {
    private final VehiculeService vehiculeService;
    private final Flotte flotte;

    public FlotteController(VehiculeService vehiculeService, Flotte flotte) {
        this.vehiculeService = vehiculeService;
        this.flotte = flotte;
    }

    public String afficherFlotte() {
        StringBuilder flotte = new StringBuilder();
        List<Vehicule> liste = vehiculeService.afficherFlotte();

        flotte.append("FLOTTE — ").append(liste.size()).append(" véhicules\n");
        flotte.append("--------------------------------------------------------------\n");
        flotte.append("Immat. | Marque | Modèle | Mise circ. | Km | État\n");
        flotte.append("--------------------------------------------------------------\n");

        for (Vehicule v : liste) {
            String etat = vehiculeService.estEnDeplacement(v) ? "En déplacement" : "Disponible";
            flotte.append(v.getImmatriculation()).append(" | ")
                    .append(v.getMarque()).append(" | ")
                    .append(v.getModele()).append(" | ")
                    .append(v.getdateMiseCirculation()).append(" | ")
                    .append(v.getKilometrage()).append(" km | ")
                    .append(etat).append("\n");
        }

        flotte.append("--------------------------------------------------------------\n");
        return flotte.toString();
    }

    public String ajouterVehicule(String immatriculation, String marque, String modele, LocalDate dateMiseCirculation, int kilometrage) {
        try {
            Vehicule v = new Vehicule(immatriculation, marque, modele, dateMiseCirculation, kilometrage);
            vehiculeService.ajouterVehicule(v);
            return "Véhicule " + immatriculation.toUpperCase() + " ajouté. Enregistrement en base en cours…";
        } catch (RegleGestionException | IllegalArgumentException e) {
            return "Refusé : " + e.getMessage();
        }
    }

    public String retirerVehicule(String immatriculation) {
        try {
            vehiculeService.retirerVehicule(immatriculation);
            return "Véhicule " + immatriculation.toUpperCase() + " retiré de la flotte";
        } catch (RegleGestionException e) {
            return "Refusé : " + e.getMessage();
        }
    }

    public String rechercherVehicule(String immatriculation) {
        try {
            Vehicule v = vehiculeService.rechercherParImmatriculation(immatriculation);
            return v.getImmatriculation() + " " + v.getMarque() + " " + v.getModele()
                    + " (mis en circulation le " + v.getdateMiseCirculation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + ", " + v.getKilometrage() + " km)\n"
                    + "État : " + (vehiculeService.estEnDeplacement(v) ? "en déplacement" : "disponible");
        } catch (RegleGestionException e) {
            return "Refusé : " + e.getMessage();
        }
    }

    public String trier(int critere) {
        List<Vehicule> liste;
        String titre;

        switch (critere) {
            case 1 -> {
                liste = vehiculeService.trierParMarqueModele();
                titre = ("FLOTTE TRIÉE PAR MARQUE PUIS MODELE");
            }
            case 2 -> {
                liste = vehiculeService.trierParDate();
                titre = ("FLOTTE TRIÉE PAR DATE DE MISE EN CIRCULATION (plus ancien d'abord)");
            }
            case 3 -> {
                liste = vehiculeService.trierParKmDecroissant();
                titre = ("FLOTTE TRIÉE PAR KILOMÉTRAGE DÉCROISSANT");
            }
            default -> {
                return "Tri invalide";
            }

        }
        StringBuilder vehicule = new StringBuilder();
        vehicule.append(titre).append("\n");
        for (Vehicule v : liste) {
            vehicule.append(v.getImmatriculation()).append(" ").append(v.getMarque()).append(" ").append(v.getModele()).append(" ").append(v.getKilometrage()).append(" km").append("\n");
        }
        return vehicule.toString();
    }

}
