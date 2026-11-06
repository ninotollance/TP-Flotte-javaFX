package cda.vehicules.DAO;

import cda.vehicules.exception.DAOException;
import cda.vehicules.modele.Reservation;
import cda.vehicules.modele.Reservations;
import cda.vehicules.modele.Salarie;

import java.sql.*;
import java.util.*;

public class ReservationsDAO extends DAO implements IReservationsDAO<Salarie, Timestamp, Reservation> {

    private static final String TABLE = "Reservation";
    private static final String COL_ID = "id";
    private static final String COL_MAT = "matricule";
    private static final String COL_ID_VEHICULE = "id_vehicule";
    private static final String COL_DATE_DEPART = "date_depart";
    private static final String COL_DATE_RETOUR = "date_retour";
    private static final String COL_KM_RETOUR = "km_retour";

    public ReservationsDAO(DAOFactory factory) {
        super(factory);
    }

    @Override
    public Reservations<Salarie, Timestamp, Reservation> create(Reservations<Salarie, Timestamp, Reservation> r) {

        String query = "INSERT INTO " + TABLE + " ("
                + COL_MAT + ", " + COL_ID_VEHICULE + ", " + COL_DATE_DEPART + ", "
                + COL_DATE_RETOUR + ", " + COL_KM_RETOUR
                + ") VALUES (?, ?, ?, ?, ?);";

        Salarie s = r.getSalarie();
        Map<Timestamp, Reservation> map = r.getReservations();

        map.forEach((tstp, res) -> {
            try (PreparedStatement pst = getConnexion().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pst.setString(1, s.getMatricule());
                pst.setInt(2, res.getVehicule().getId());
                pst.setTimestamp(3, tstp);
                pst.setTimestamp(4, res.getDate_retour());
                pst.setInt(5, res.getKm_retour());
                if (pst.executeUpdate() <= 0) {
                    throw new DAOException("Erreur lors de la création de la réservation.");
                }
            } catch (SQLException e) {
                throw new DAOException("Erreur SQL lors de la création de la réservation", e);
            }
        });
        return r;
    }

    @Override
    public Reservations<Salarie, Timestamp, Reservation> read(String matricule) {

        String query = "SELECT * FROM " + TABLE + " WHERE " + COL_MAT + "=?;";

        ISalarieDAO salarieDAO = new SalarieDAO(DAOFactory.getInstance());
        IVehiculeDAO vehiculeDAO = new VehiculeDAO(DAOFactory.getInstance());
        Salarie salarie = salarieDAO.read(matricule);
        Map<Timestamp, Reservation> map = new TreeMap<>();
        Reservations<Salarie, Timestamp, Reservation> r = new Reservations<>(salarie);
        try (PreparedStatement pst = getConnexion().prepareStatement(query)) {
            pst.setString(1, matricule);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Reservation res = new Reservation(
                        vehiculeDAO.read(rs.getInt(COL_ID_VEHICULE)),
                        rs.getTimestamp(COL_DATE_DEPART),
                        rs.getTimestamp(COL_DATE_RETOUR),
                        rs.getInt(COL_KM_RETOUR)
                );
                map.put(rs.getTimestamp(COL_DATE_DEPART), res);
            }
            r.setReservations(map);
            return r;
         } catch (SQLException e) {
            throw new DAOException("Erreur SQL lors de la lecture de reservation de " + matricule, e);
        }
    }



    @Override
    public List<Reservations<Salarie, Timestamp, Reservation>> readAll() {
        List<Reservations<Salarie, Timestamp, Reservation>> lRes = new ArrayList<>();
        ISalarieDAO salarieDAO = new SalarieDAO(DAOFactory.getInstance());
        List<Salarie> lSal = salarieDAO.readAll();
        for (Salarie s : lSal) {
            lRes.add(read(s.getMatricule()));
        }
        return lRes;
    }

    @Override
    public boolean update(Reservations<Salarie, Timestamp, Reservation> r) {

        String query = "UPDATE " + TABLE + " SET "
                + COL_MAT + " = ?, "
                + COL_ID_VEHICULE + " = ?, "
                + COL_DATE_RETOUR + " = ?, "
                + COL_KM_RETOUR + " = ? "
                + "WHERE " + COL_DATE_DEPART + " = ?;";

        Salarie salarie = r.getSalarie();
        Map<Timestamp, Reservation> map = r.getReservations();

        map.forEach((tstp, res) -> {
            try (PreparedStatement pst = getConnexion().prepareStatement(query)) {
                pst.setString(1, salarie.getMatricule());
                pst.setInt(2, res.getVehicule().getId());
                pst.setTimestamp(3, res.getDate_retour());
                pst.setInt(4, res.getKm_retour());
                pst.setTimestamp(5, tstp);
                pst.executeUpdate();
            } catch (SQLException e) {
                throw new DAOException("Erreur SQL lors de la mise à jour des reservations de " + salarie.getMatricule(), e);
            }
        });
        return true;
    }

    @Override
    public boolean delete(int id) {

        String query = "DELETE FROM " + TABLE + " WHERE " + COL_ID + "=?;";

        try (PreparedStatement pst = getConnexion().prepareStatement(query)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Erreur SQL lors de la suppression de la reservation " + id, e);
        }
    }

    @Override
    public boolean estEnDeplacement(int idVehicule) {

        String query = "SELECT EXISTS (SELECT 1 FROM " + TABLE +
                " WHERE " + COL_ID_VEHICULE + "=? AND " + COL_DATE_RETOUR + " IS NULL);";

        try (PreparedStatement pst = getConnexion().prepareStatement(query)) {
            pst.setInt(1, idVehicule);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur SQL lors de la recherche déplacement de la reservation pour le vehicule " + idVehicule, e);
        }
    }



    @Override
    public Integer trouverVehiculeEnCours(String matricule) {

        String query = "SELECT " + COL_ID_VEHICULE + " FROM " + TABLE +
                " WHERE " + COL_MAT + "=? AND " + COL_DATE_RETOUR + " IS NULL";

        try (PreparedStatement pst = getConnexion().prepareStatement(query)) {
            pst.setString(1, matricule);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_vehicule");
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur SQL lors de la recherche déplacement de la reservation pour le salarié " + matricule, e);
        }
    }

    @Override
    public boolean cloturer(int idVehicule, Timestamp t, int km) {

        String query1 = "SELECT " + COL_ID + " FROM " + TABLE +
                " WHERE " + COL_ID_VEHICULE + "=? AND " + COL_DATE_RETOUR + " IS NULL;";

        String query2 = "UPDATE " + TABLE + " SET " + COL_DATE_RETOUR + "=?, " + COL_KM_RETOUR + "=? WHERE " +
                COL_ID + "=?;";

        int resId;

        try (PreparedStatement pst1 = getConnexion().prepareStatement(query1)) {
            pst1.setInt(1, idVehicule);
            ResultSet rs = pst1.executeQuery();
            if (rs.next()) {
                resId = rs.getInt(1);
                try (PreparedStatement pst2 = getConnexion().prepareStatement(query2)) {
                    pst2.setTimestamp(1, t);
                    pst2.setInt(2, km);
                    pst2.setInt(3, resId);
                    return pst2.executeUpdate() > 0;
                }
            }
            else throw new DAOException("Erreur SQL lors de la cloture de la reservation pour le vehicule " + idVehicule);
        } catch (SQLException e) {
            throw new DAOException("Erreur SQL lors de la cloture de la reservation pour le vehicule " + idVehicule, e);
        }
    }
}
