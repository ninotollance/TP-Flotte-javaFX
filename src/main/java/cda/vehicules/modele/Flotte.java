package cda.vehicules.modele;

import java.util.ArrayList;
import java.util.List;

public class Flotte {

    private static List<Vehicule> vehicules;

    public Flotte() {
        this.vehicules = new ArrayList<>();
    }

    public void ajouter(Vehicule v) {
        vehicules.add(v);
    }

    public List<Vehicule> getVehicules() {
        return vehicules;
    }

    public void setVehicules(List<Vehicule> vehicules) {
        this.vehicules = vehicules;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Flotte :\n");
        for (Vehicule v : vehicules)
            s.append(v);
        return s.toString();
    }
}
