package cda.vehicules.DAO;

import cda.vehicules.modele.Salarie;

import java.util.List;

public interface ISalarieDAO {
    Salarie create(Salarie s);
    Salarie read(String matricule);
    List<Salarie> readAll();
    boolean update(Salarie s);
    boolean delete(String matricule);
}
