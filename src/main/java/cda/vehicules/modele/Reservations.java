package cda.vehicules.modele;

import java.util.Map;
import java.util.TreeMap;

public class Reservations<S, K, V>{

    private S salarie ;
    private Map<K, V> reservations;

    public Reservations(S salarie) {
        this.salarie = salarie;
        this.reservations = new TreeMap<>();
    }

    public S getSalarie() {
        return salarie;
    }

    public void setSalarie(S salarie) {
        this.salarie = salarie;
    }

    public Map<K, V> getReservations() {
        return reservations;
    }

    public void setReservations(Map<K, V> reservations) {
        this.reservations = reservations;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(salarie).append(" :");

        if (reservations == null || reservations.isEmpty()) {
            sb.append("\n      (aucune réservation)");
            return sb.toString();
        }

        for (V reservation : reservations.values()) {
            sb.append("\n      ").append(reservation);
        }
        return sb.toString();
    }
}
