package cda.vehicules.service;

import cda.vehicules.DAO.IReservationsDAO;
import cda.vehicules.DAO.ReservationsDAO;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import cda.vehicules.modele.Flotte;
import cda.vehicules.modele.Reservation;
import cda.vehicules.modele.Salarie;
import cda.vehicules.modele.Vehicule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;

public class PdfService {

    private static final Logger log = LogManager.getLogger(VehiculeService.class);

    Flotte flotte;
    ReservationsDAO reservationsDAO;
    String nomFichier;

    public PdfService(IReservationsDAO<Salarie, Timestamp, Reservation> reservationsDAO, Flotte flotte, String nomFichier) {
        this.reservationsDAO = (ReservationsDAO) reservationsDAO;
        this.flotte = flotte;
        this.nomFichier = nomFichier;
    }

    private String genEtatFlotte() {
        log.info("Création du contenu du fichier pdf de l'état de la flotte");
        StringBuilder s = new StringBuilder();
        s.append("État de la flotte de véhicule le ");
        s.append(LocalDate.now());
        s.append("\n\n");
        for (Vehicule v : flotte.getVehicules()) {
            s.append(v);
            String dispo = reservationsDAO.estEnDeplacement(v.getId()) ? " | en déplacement" : " | disponible";
            s.append(dispo);
            s.append("\n");
        }
        return s.toString();
    }

    public void exportPDF() throws IOException {
        //gestionnaire de l'ecriture dans le fichier pdf
        PdfWriter writer = new PdfWriter(this.nomFichier);
        //creation d'un document avec plusieurs pages
        PdfDocument pdfDoc = new PdfDocument(writer);
        //creation du contenu de la page du document
        Document doc = new Document(pdfDoc);

        PdfFont myFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        Paragraph p = new Paragraph();
        p.add(this.genEtatFlotte());
        doc.add(p);
        doc.close();
    }

}
