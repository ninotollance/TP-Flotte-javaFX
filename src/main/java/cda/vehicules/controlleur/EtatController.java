package cda.vehicules.controlleur;

import cda.vehicules.exception.MailException;
import cda.vehicules.service.ExportService;
import cda.vehicules.service.MailService;
import cda.vehicules.service.PdfService;
import cda.vehicules.utils.ChiffrementAES;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class EtatController {

    ExportService exportService;
    PdfService pdfService;
    MailService mailService;

    private String sujet = "Etat de la flotte";
    private String message = "Ci-joint l'état de la flotte de véhicules.";

    public EtatController(ExportService exportService, PdfService pdfService, MailService mailService) {
        this.exportService = exportService;
        this.pdfService = pdfService;
        this.mailService = mailService;
    }

    public void genPdfMail() throws IOException, MailException {
        pdfService.exportPDF();
        mailService.envoyerMail(sujet, message);
    }

    public void exporter() throws GeneralSecurityException, IOException {
        exportService.exportSalaries("salaries.enc");
        exportService.exportReservations("reservations.enc");
    }

    public String lireFichierExport() throws IOException, GeneralSecurityException {
        String salaries = ChiffrementAES.dechiffrer(
                ChiffrementAES.chargerCle(exportService.getKeyFile()),
                exportService.getRepertoireExport().resolve("salaries.enc")
                );
        String reservations = ChiffrementAES.dechiffrer(
                ChiffrementAES.chargerCle(exportService.getKeyFile()),
                exportService.getRepertoireExport().resolve("reservations.enc")
        );
        return salaries + "\n" + reservations;
    }
}
