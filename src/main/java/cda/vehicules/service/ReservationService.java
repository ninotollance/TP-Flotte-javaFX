package cda.vehicules.service;

import cda.vehicules.DAO.IReservationsDAO;
import cda.vehicules.DAO.ISalarieDAO;
import cda.vehicules.DAO.IVehiculeDAO;
import cda.vehicules.exception.DAOException;
import cda.vehicules.exception.RegleGestionException;
import cda.vehicules.modele.*;


import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReservationService {

    private static final Logger log = LogManager.getLogger(ReservationService.class);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private List<Reservations<Salarie, Timestamp, Reservation>> lesReservations = new ArrayList<>();

    private final ISalarieDAO salarieDAO;
    private final IReservationsDAO<Salarie, Timestamp, Reservation> reservationsDAO;
    private final IVehiculeDAO vehiculeDAO;
    private final Flotte flotte;

    public ReservationService(ISalarieDAO salarieDAO, IReservationsDAO<Salarie, Timestamp, Reservation> reservationsDAO, IVehiculeDAO vehiculeDAO, Flotte flotte) {
        this.salarieDAO = salarieDAO;
        this.reservationsDAO = reservationsDAO;
        this.vehiculeDAO = vehiculeDAO;
        this.flotte = flotte;
        lesReservations.addAll(reservationsDAO.readAll());

    }

    /** Renvoie le carnet de réservations du salarié qui a ce matricule, ou null s'il n'existe pas. */
    private Reservations<Salarie, Timestamp, Reservation> trouverCarnet(String matricule) {
        for (Reservations<Salarie, Timestamp, Reservation> r : lesReservations) {
            if(r.getSalarie().getMatricule().equals(matricule)) {
                return r;
            }
        }
        return null;
    }

    /** Retourne le véhicule correspondant à l'immatriculation ou null */
    private Vehicule lireVehicule(String immatriculation) {
        try {
            return vehiculeDAO.read(immatriculation);
        } catch (DAOException e) {
            return null;
        }
    }

    /** Crée un salarié, vérifie que son matricule n'existe pas déjà, lui crée un carnet vide en mémoire */
    public void creerSalarie(Salarie s) {
        if (s == null) {
            log.warn("Ajout refusé : aucun salarié fourni");
            throw new RegleGestionException("Aucun salarié fourni !");
        }
        for (Reservations<Salarie, Timestamp, Reservation> r : lesReservations) {
            if (r.getSalarie().getMatricule().equals(s.getMatricule())) {
                log.warn("Ajout refusé : le matricule {} existe déjà", s.getMatricule());
                throw new RegleGestionException ("Le matricule " + s.getMatricule() + " existe déjà");
            }
        }
        lesReservations.add(new Reservations<>(s));

        executor.submit(() -> {
            try {
                salarieDAO.create(s);
                log.info("Salarié enregistré en base : {}", s.getMatricule());
            } catch (DAOException e) {
                log.error("Échec de l'enregistrement de {}", s.getMatricule(), e);
            }
        });
    }

    /** Enregistre le départ d'un véhicule avec un salarié  */
    public void enregistrerDepart(String matricule, String immatriculation) {
        Reservations<Salarie, Timestamp, Reservation> resa = trouverCarnet(matricule);

        if (resa == null) {
            log.warn("Départ refusé : le matricule {} n'existe pas", matricule);
            throw new RegleGestionException ("Le matricule " + matricule + " n'existe pas");
        }

        Vehicule v = lireVehicule(immatriculation);

        if (v == null) {
            log.warn("Départ refusé : véhicule inconnu");
            throw new RegleGestionException("véhicule inconnu");
        }
        if (reservationsDAO.estEnDeplacement(v.getId())) {
            log.warn("Départ refusé : véhicule en déplacement");
            throw new RegleGestionException("ce véhicule est en déplacement");
        }

        Integer idEnCours = reservationsDAO.trouverVehiculeEnCours(matricule);
        if(idEnCours != null) {
            log.warn("Départ refusé : {} a déjà un véhicule en déplacement", matricule);
            throw new RegleGestionException("Ce salarié a déjà un véhicule en déplacement");
        }

        Timestamp maintenant = new Timestamp(System.currentTimeMillis());
        Reservation nouvelle = new Reservation(v, maintenant, null, 0);

        resa.getReservations().put(maintenant, nouvelle);

        Reservations<Salarie, Timestamp, Reservation> aInserer = new Reservations<>(resa.getSalarie());
        aInserer.getReservations().put(maintenant, nouvelle);

        executor.submit(() -> {
            try {
                reservationsDAO.create(aInserer);
                log.info("Départ enregistré : {} avec {}", immatriculation, matricule);
            } catch (DAOException e) {
                log.error("Échec de l'enregistrement du départ de {}", immatriculation, e);
            }
        });

    }

    /** Enregistre le retour d'un véhicule */
    public int enregistrerRetour(String immatriculation, int kmRetour) {
        Vehicule v = lireVehicule(immatriculation);

        if (v == null) {
            log.warn("Retour refusé : véhicule inconnu");
            throw new RegleGestionException("véhicule inconnu");
        }
        if (!reservationsDAO.estEnDeplacement(v.getId())) {
            log.warn("Retour refusé : véhicule pas en déplacement");
            throw new RegleGestionException("ce véhicule n'est pas en déplacement");
        }
        if (kmRetour < v.getKilometrage()) {
            log.warn("Retour refusé pour {} : kilométrage inférieur au compteur ({} km)", immatriculation, v.getKilometrage());
            throw new RegleGestionException("kilométrage inférieur au compteur actuel " + v.getKilometrage());
        }

        Timestamp maintenant = new Timestamp(System.currentTimeMillis());
        reservationsDAO.cloturer(v.getId(), maintenant, kmRetour);
        int kmParcourus = kmRetour - v.getKilometrage();
        v.setKilometrage(kmRetour);
        vehiculeDAO.update(v);

        for (Vehicule fv : flotte.getVehicules()) {
            if (fv.getId() == v.getId()) {
                fv.setKilometrage(kmRetour);
            }
        }

        /** Met à jour la mémoire : cloturer() n'a modifié que la base. */
        for (Reservations<Salarie, Timestamp, Reservation> carnet : lesReservations) {
            for (Map.Entry<Timestamp, Reservation> e : carnet.getReservations().entrySet()) {
                Reservation res = e.getValue();
                if (res.getDate_retour() == null && res.getVehicule().getId() == v.getId()) {
                    e.setValue(new Reservation(res.getVehicule(), e.getKey(), maintenant, kmRetour));
                }
            }
        }

        log.info("Retour de {} à {} km", immatriculation, kmRetour);

        return kmParcourus;
    }

    /** historique des réservations d'un salarié  */
    public Map<Timestamp, Reservation> historique(String matricule) {
        Reservations<Salarie, Timestamp, Reservation> resa = trouverCarnet(matricule);
        if (resa == null) {
            log.warn("Historique refusé : le matricule {} n'existe pas", matricule);
            throw new RegleGestionException("Cet historique n'est pas disponible, le matricule " + matricule + " n'existe pas !");
        }
        log.info("Historique {} consulté", matricule);
        Map<Timestamp, Reservation> trie = new TreeMap<>(Comparator.reverseOrder());
        trie.putAll(resa.getReservations());
        return trie;
    }

    /** liste des véhicules actuellement en déplacement. */
    public List<Vehicule> vehiculeEnDeplacement() {
        List<Vehicule> enDeplacement = new ArrayList<>();

        for (Vehicule v : vehiculeDAO.readAll()) {
            if (reservationsDAO.estEnDeplacement(v.getId())) {
                enDeplacement.add(v);
            }
        }
        log.info("Consultation des véhicules en déplacement : {}", enDeplacement.size());
        return enDeplacement;
    }

    public void fermer() throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
            executor.shutdownNow();
        }
    }

}
