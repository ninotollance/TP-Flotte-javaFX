package cda.vehicules.DAO;

import cda.vehicules.modele.Vehicule;

import java.util.List;

public interface IVehiculeDAO {
    Vehicule create(Vehicule v);
    Vehicule read(int id);
    Vehicule read(String immat);
    List<Vehicule> readAll();
    boolean update(Vehicule v);
    boolean delete(int id);
}
