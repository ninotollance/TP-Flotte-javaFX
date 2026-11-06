package cda.vehicules.DAO;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class DAO {

    protected final DAOFactory factory;

    protected DAO(DAOFactory factory) {
        this.factory = factory;
    }

    protected Connection getConnexion() throws SQLException {
        return factory.getConnexion();
    }
}