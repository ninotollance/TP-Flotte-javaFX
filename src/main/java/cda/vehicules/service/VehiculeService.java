package cda.vehicules.service;

import cda.vehicules.DAO.IReservationsDAO;
import cda.vehicules.DAO.IVehiculeDAO;
import cda.vehicules.exception.DAOException;
import cda.vehicules.exception.RegleGestionException;
import cda.vehicules.modele.Flotte;
import cda.vehicules.modele.Vehicule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VehiculeService {

    private static final Logger log = LogManager.getLogger(VehiculeService.class);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final Comparator<Vehicule> PAR_MARQUE_MODELE =
            Comparator.comparing(Vehicule::getMarque).thenComparing(Vehicule::getModele);
    private static final Comparator<Vehicule> PAR_DATE =
            Comparator.comparing(Vehicule::getdateMiseCirculation);
    private static final Comparator<Vehicule> PAR_KM_DECROISSANT =
            Comparator.comparingInt(Vehicule::getKilometrage).reversed();

    private final IVehiculeDAO vehiculeDAO;
    private final IReservationsDAO reservationsDAO;
    private final Flotte flotte;

    public VehiculeService(IVehiculeDAO vehiculeDAO, IReservationsDAO reservationsDAO, Flotte flotte) {
        this.vehiculeDAO = vehiculeDAO;
        this.reservationsDAO = reservationsDAO;
        this.flotte = flotte;
        try {
            flotte.setVehicules(vehiculeDAO.readAll());
            log.info("Flotte chargée : {} véhicules", flotte.getVehicules().size());
        } catch (DAOException e) {
            log.error("Échec du chargement de la flotte", e);
            throw e;
        }
    }

    public void ajouterVehicule(Vehicule v) {
        if (v == null) {
            log.warn("Ajout refusé : aucun véhicule fourni");
            throw new RegleGestionException("aucun véhicule fourni");
        }
        for (Vehicule existant : flotte.getVehicules()) {
            if (existant.getImmatriculation().equalsIgnoreCase(v.getImmatriculation())) {
                log.warn("Ajout refusé : l'immatriculation {} existe déjà", v.getImmatriculation());
                throw new RegleGestionException("l'immatriculation " + v.getImmatriculation() + " existe déjà");
            }
        }

        flotte.ajouter(v);
        log.info("Véhicule ajouté à la flotte : {}", v.getImmatriculation());

        executor.submit(() -> {
            try {
                vehiculeDAO.create(v);
                log.info("Véhicule enregistré en base : {}", v.getImmatriculation());
            } catch (DAOException e) {
                log.error("Échec de l'enregistrement de {}", v.getImmatriculation(), e);
            }
        });
    }

    public Vehicule rechercherParImmatriculation(String immat) {
        for (Vehicule v : flotte.getVehicules()) {
            if (v.getImmatriculation().equalsIgnoreCase(immat)) {
                log.info("Recherche de {} : véhicule trouvé", immat);
                return v;
            }
        }
        log.warn("Recherche de {} : véhicule inconnu", immat);
        throw new RegleGestionException("véhicule inconnu : " + immat);
    }

    public List<Vehicule> afficherFlotte() {
        log.info("Affichage de la flotte : {} véhicules", flotte.getVehicules().size());
        return flotte.getVehicules();
    }

    public boolean estEnDeplacement(Vehicule v) {
        return reservationsDAO.estEnDeplacement(v.getId());
    }

    public void retirerVehicule(String immat) {
        Vehicule v = rechercherParImmatriculation(immat);

        if (reservationsDAO.estEnDeplacement(v.getId())) {
            log.warn("Retrait refusé : le véhicule {} est en déplacement", v.getImmatriculation());
            throw new RegleGestionException("le véhicule " + v.getImmatriculation()
                    + " est en déplacement. Enregistrez son retour d'abord.");
        }

        flotte.getVehicules().remove(v);
        log.info("Véhicule retiré de la flotte : {}", v.getImmatriculation());

        executor.submit(() -> {
            try {
                vehiculeDAO.delete(v.getId());
                log.info("Véhicule supprimé en base : {}", v.getImmatriculation());
            } catch (DAOException e) {
                log.error("Échec de la suppression de {}", v.getImmatriculation(), e);
            }
        });
    }

    public List<Vehicule> trierParMarqueModele() {
        List<Vehicule> copie = new ArrayList<>(flotte.getVehicules());
        copie.sort(PAR_MARQUE_MODELE);
        log.info("Tri de la flotte par marque et modèle");
        return copie;
    }

    public List<Vehicule> trierParDate() {
        List<Vehicule> copie = new ArrayList<>(flotte.getVehicules());
        copie.sort(PAR_DATE);
        log.info("Tri de la flotte par date de mise en circulation");
        return copie;
    }

    public List<Vehicule> trierParKmDecroissant() {
        List<Vehicule> copie = new ArrayList<>(flotte.getVehicules());
        copie.sort(PAR_KM_DECROISSANT);
        log.info("Tri de la flotte par kilométrage décroissant");
        return copie;
    }

    public void fermer() throws InterruptedException {
        log.info("Arrêt du service véhicules");
        executor.shutdown();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            log.warn("Arrêt forcé : des écritures en base n'étaient pas terminées");
            executor.shutdownNow();
        }
    }
}
