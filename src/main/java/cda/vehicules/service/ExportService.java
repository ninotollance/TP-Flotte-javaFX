package cda.vehicules.service;

import cda.vehicules.DAO.IReservationsDAO;
import cda.vehicules.DAO.ISalarieDAO;
import cda.vehicules.modele.Reservation;
import cda.vehicules.modele.Reservations;
import cda.vehicules.modele.Salarie;
import cda.vehicules.utils.ChiffrementAES;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import java.sql.Timestamp;
import java.util.List;

public class ExportService {

    ISalarieDAO salarieDAO;
    IReservationsDAO<Salarie, Timestamp, Reservation> reservationsDAO;
    Path repertoireExport;
    Path keyFile;

    public ExportService(ISalarieDAO salarieDAO,
                         IReservationsDAO<Salarie, Timestamp, Reservation> reservationsDAO,
                         Path repertoireExport,
                         Path keyFile) {
        this.salarieDAO = salarieDAO;
        this.reservationsDAO = reservationsDAO;
        this.repertoireExport = repertoireExport;
        this.keyFile = keyFile;
    }

    public void exportSalaries(String nomFichier) throws IOException, GeneralSecurityException {
        List<Salarie> l = salarieDAO.readAll();
        StringBuilder salaries = new StringBuilder();
        for (Salarie s : l) {
            salaries.append(s.getMatricule());
            salaries.append(";");
            salaries.append(s.getNom());
            salaries.append(";");
            salaries.append(s.getPrenom());
            salaries.append("\n");
        }
        ChiffrementAES.chiffrer(
                    salaries.toString(),
                    ChiffrementAES.chargerCle(keyFile),
                    repertoireExport.resolve(nomFichier)
                );
    }

    public void exportReservations(String nomFichier) throws IOException, GeneralSecurityException {
        List<Reservations<Salarie, Timestamp, Reservation>> l = reservationsDAO.readAll();
        StringBuilder res = new StringBuilder();
        for (Reservations<Salarie, Timestamp, Reservation> r : l) {
            r.getReservations().forEach((tstp, reservation) -> {
            res.append(r.getSalarie().getMatricule());
            res.append(";");
            res.append(reservation.getVehicule().getImmatriculation());
            res.append(";");
            res.append(reservation.getDate_depart());
                res.append(";");
            if (reservation.getDate_retour() != null)
                res.append(reservation.getDate_retour());
            res.append(";");
            if (reservation.getKm_retour() != 0)
                res.append(reservation.getKm_retour());
            res.append("\n");
            });
        }
        ChiffrementAES.chiffrer(
                res.toString(),
                ChiffrementAES.chargerCle(keyFile),
                repertoireExport.resolve(nomFichier)
        );
    }

    public Path getRepertoireExport() {
        return repertoireExport;
    }

    public void setRepertoireExport(Path repertoireExport) {
        this.repertoireExport = repertoireExport;
    }

    public Path getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(Path keyFile) {
        this.keyFile = keyFile;
    }

}
