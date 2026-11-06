package cda.vehicules.DAO;

import cda.vehicules.modele.Reservations;

import java.sql.Timestamp;
import java.util.List;

public interface IReservationsDAO<S, K, V> {
    Reservations<S, K, V> create(Reservations<S, K, V> r);
    Reservations<S, K, V> read(String matricule);
    List<Reservations<S, K, V>> readAll();
    boolean update(Reservations<S, K, V> r);
    boolean delete(int id);

    boolean estEnDeplacement(int idVehicule);
    Integer trouverVehiculeEnCours(String matricule);
    boolean cloturer(int idVehicule, Timestamp t, int km);
}
