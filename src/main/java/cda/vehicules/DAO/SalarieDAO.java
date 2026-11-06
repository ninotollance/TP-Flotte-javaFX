package cda.vehicules.DAO;

import cda.vehicules.exception.DAOException;
import cda.vehicules.modele.Salarie;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalarieDAO extends DAO implements ISalarieDAO {

    // Noms de la table et des colonnes
    private static final String TABLE = "Salarie";
    private static final String COL_MATRICULE = "matricule";
    private static final String COL_NOM = "nom";
    private static final String COL_PRENOM = "prenom";

    public SalarieDAO(DAOFactory factory) {
        super(factory);
    }

    @Override
    public Salarie create(Salarie s) {
        String query = "INSERT INTO " + TABLE + " ("
                + COL_MATRICULE + ", " + COL_NOM + ", " + COL_PRENOM
                + ") VALUES (?, ?, ?)";

        try (PreparedStatement pst = getConnexion().prepareStatement(query)) {
            pst.setString(1, s.getMatricule());
            pst.setString(2, s.getNom());
            pst.setString(3, s.getPrenom());

            if (pst.executeUpdate() <= 0) {
                throw new DAOException("Erreur lors de la création du salarié.");
            }
            return s;
        } catch (SQLException e) {
            throw new DAOException("Erreur SQL lors de la création du salarié " + s.getMatricule(), e);
        }
    }

    @Override
    public Salarie read(String matricule) {
        String query = "SELECT * FROM " + TABLE + " WHERE " + COL_MATRICULE + " = ?";

        try (PreparedStatement pst = getConnexion().prepareStatement(query)) {
            pst.setString(1, matricule);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapper(rs);
                }
                throw new DAOException("Le salarié de matricule " + matricule + " n'a pas été trouvé.");
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur SQL lors de la lecture du salarié " + matricule, e);
        }
    }

    @Override
    public List<Salarie> readAll() {
        String query = "SELECT * FROM " + TABLE;
        List<Salarie> liste = new ArrayList<>();

        try (PreparedStatement pst = getConnexion().prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                liste.add(mapper(rs));
            }
            return liste;
        } catch (SQLException e) {
            throw new DAOException("Erreur SQL lors de la lecture des salariés", e);
        }
    }

    @Override
    public boolean update(Salarie s) {
        String query = "UPDATE " + TABLE + " SET "
                + COL_NOM + " = ?, "
                + COL_PRENOM + " = ? "
                + "WHERE " + COL_MATRICULE + " = ?";

        try (PreparedStatement pst = getConnexion().prepareStatement(query)) {
            pst.setString(1, s.getNom());
            pst.setString(2, s.getPrenom());
            pst.setString(3, s.getMatricule());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Erreur SQL lors de la mise à jour du salarié " + s.getMatricule(), e);
        }
    }

    @Override
    public boolean delete(String matricule) {
        String query = "DELETE FROM " + TABLE + " WHERE " + COL_MATRICULE + " = ?";

        try (PreparedStatement pst = getConnexion().prepareStatement(query)) {
            pst.setString(1, matricule);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Erreur SQL lors de la suppression du salarié " + matricule, e);
        }
    }

    // Construit un Salarie à partir de la ligne courante du ResultSet
    private Salarie mapper(ResultSet rs) throws SQLException {
        return new Salarie(
                rs.getString(COL_NOM),
                rs.getString(COL_PRENOM),
                rs.getString(COL_MATRICULE)
        );
    }
}
