package cda.vehicules.service;

import cda.vehicules.exception.MailException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MailService {

    private static final Logger log = LogManager.getLogger(VehiculeService.class);

    private String cleApi;
    private String envoyeur;
    private String receveur;

    private final String CONSTANTES_PROPERTIES  = "configMail.properties";
    private final String EXPORTPDF = "exports/etatFlotte.pdf";

    public MailService() {
        appliquerConfig();
    }

    public boolean envoyerMail(String sujet, String message) throws MailException {
        log.info("Envoi du mail avec l'état de la flotte");
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey(this.cleApi);

        try {
            TransactionalEmailsApi api = new TransactionalEmailsApi();

            // Envoyeur
            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(this.envoyeur);
            sender.setName("Simon MAHEO");

            // Receveur
            List<SendSmtpEmailTo> toList = new ArrayList<>();
            SendSmtpEmailTo to = new SendSmtpEmailTo();
            to.setEmail(this.receveur);
            to.setName("Pascal Lamy");
            toList.add(to);

            // Piece-jointe
            SendSmtpEmailAttachment attachment = new SendSmtpEmailAttachment();
            attachment.setName("etatFlotte.pdf");
            byte[] encode = Files.readAllBytes(Paths.get(EXPORTPDF));
            attachment.setContent(encode);
            List<SendSmtpEmailAttachment> attachmentList = new ArrayList<>();
            attachmentList.add(attachment);

            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(toList);
            sendSmtpEmail.setHtmlContent(message);
            sendSmtpEmail.setSubject(sujet);
            sendSmtpEmail.setAttachment(attachmentList);

            CreateSmtpEmail response = api.sendTransacEmail(sendSmtpEmail);
            log.info("Mail envoyé");
            return response.getMessageId() != null; // comment savoir si le mail est bien envoyé ?

        } catch (Exception e) {
            throw new MailException("Erreur lors de l'envoi du mail");
        }

    }

    private void appliquerConfig() {
        Properties props = new Properties();

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(CONSTANTES_PROPERTIES)) {

            if (in == null)
                throw new IllegalStateException(CONSTANTES_PROPERTIES + " introuvable dans le classpath.");

            props.load(in);

        } catch (IOException e) {
            throw new RuntimeException("Erreur de lecture du fichier", e);
        }

        this.cleApi = props.getProperty("cleApi");
        this.envoyeur = props.getProperty("envoyeur");
        this.receveur = props.getProperty("receveur");
        log.info("Application de la configuration du mail");
    }

    public String getCleApi() {
        return cleApi;
    }

    public void setCleApi(String cleApi) {
        this.cleApi = cleApi;
    }

    public String getEnvoyeur() {
        return envoyeur;
    }

    public void setEnvoyeur(String envoyeur) {
        this.envoyeur = envoyeur;
    }

    public String getReceveur() {
        return receveur;
    }

    public void setReceveur(String receveur) {
        this.receveur = receveur;
    }
}

