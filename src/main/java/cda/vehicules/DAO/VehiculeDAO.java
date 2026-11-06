package cda.vehicules.DAO;

import cda.vehicules.exception.DAOException;
import cda.vehicules.modele.Vehicule;


import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VehiculeDAO extends DAO implements IVehiculeDAO {

    // Noms de la table et des colonnes
    private static final String TABLE = "Vehicule";
    private static final String COL_ID = "id";
    private static final String COL_IMMAT = "immatriculation";
    private static final String COL_MARQUE = "marque";
    private static final String COL_MODELE = "modele";
    private static final String COL_DATE_CIRCU = "date_mise_circulation";
    private static final String COL_KM = "kilometrage";

    public VehiculeDAO(DAOFactory factory) {
        super(factory);
    }

    @Override
    public Vehicule create(Vehicule v) {
        String query = "INSERT INTO " + TABLE + " ("
                + COL_IMMAT + ", " + COL_MARQUE + ", " + COL_MODELE + ", "
                + COL_DATE_CIRCU + ", " + COL_KM
                + ") VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pst = getConnexion().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, v.getImmatriculation());
            pst.setString(2, v.getMarque());
            pst.setString(3, v.getModele());
            pst.setDate(4, Date.valueOf(v.getdateMiseCirculation()));
            pst.setInt(5, v.getKilometrage());

            if (pst.executeUpdate() <= 0) {
                throw new DAOException("Erreur lors de la création du véhicule.");
            }
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    v.setId(rs.getInt(1));
                }
            }
            return v;
        } catch (SQLException e) {
            throw new DAOException("Erreur SQL lors de la création du véhicule", e);
        }
    }

    @Override
    public Vehicule read(int id) {
        String query = "SELECT * FROM " + TABLE + " WHERE " + COL_ID + " = ?";

        try (PreparedStatement pst = getConnexion().prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapper(rs);
                }
                throw new DAOException("Le véhicule à l'id " + id + " n'a pas été trouvé.");
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur SQL lors de la lecture du véhicule " + id, e);
        }
    }

    @Override
    public Vehicule read(String immat) {
        String query = "SELECT * FROM " + TABLE + " WHERE " + COL_IMMAT + " = ?";

        try (PreparedStatement pst = getConnexion().prepareStatement(query)) {
            pst.setString(1, immat);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapper(rs);
                }
                throw new DAOException("Le véhicule à l'immatriculation " + immat + " n'a pas été trouvé.");
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur SQL lors de la lecture du véhicule " + immat, e);
        }
    }

    @Override
    public List<Vehicule> readAll() {
        String query = "SELECT * FROM " + TABLE;
        List<Vehicule> liste = new ArrayList<>();

        try (PreparedStatement pst = getConnexion().prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                liste.add(mapper(rs));
            }
            return liste;
        } catch (SQLException e) {
            throw new DAOException("Erreur SQL lors de la lecture de la flotte", e);
        }
    }

    @Override
    public boolean update(Vehicule v) {
        String query = "UPDATE " + TABLE + " SET "
                + COL_IMMAT + " = ?, "
                + COL_MARQUE + " = ?, "
                + COL_MODELE + " = ?, "
                + COL_DATE_CIRCU + " = ?, "
                + COL_KM + " = ? "
                + "WHERE " + COL_ID + " = ?";

        try (PreparedStatement pst = getConnexion().prepareStatement(query)) {
            pst.setString(1, v.getImmatriculation());
            pst.setString(2, v.getMarque());
            pst.setString(3, v.getModele());
            pst.setDate(4, Date.valueOf(v.getdateMiseCirculation()));
            pst.setInt(5, v.getKilometrage());
            pst.setInt(6, v.getId());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Erreur SQL lors de la mise à jour du véhicule " + v.getId(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String query = "DELETE FROM " + TABLE + " WHERE " + COL_ID + " = ?";

        try (PreparedStatement pst = getConnexion().prepareStatement(query)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Erreur SQL lors de la suppression du véhicule " + id, e);
        }
    }

    // Construit un Vehicule à partir de la ligne courante du ResultSet
    private Vehicule mapper(ResultSet rs) throws SQLException {
        return new Vehicule(
                rs.getInt(COL_ID),
                rs.getString(COL_IMMAT),
                rs.getString(COL_MARQUE),
                rs.getString(COL_MODELE),
                rs.getObject(COL_DATE_CIRCU, LocalDate.class),
                rs.getInt(COL_KM)
        );
    }
}
