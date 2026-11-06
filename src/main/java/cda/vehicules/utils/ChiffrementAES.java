package cda.vehicules.utils;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public class ChiffrementAES {

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int TAILLE_IV = 12; // en octets
    private static final int TAILLE_TAG = 128; // contrôle d'intégrité, en bits

    private ChiffrementAES() {

    }

    /** Génère une nouvelle clé AES de 256 bits. */
    public static SecretKey genererCle() throws GeneralSecurityException {
        KeyGenerator generateur = KeyGenerator.getInstance("AES");
        generateur.init(256);
        return generateur.generateKey();
    }

    /** Écrit la clé dans un fichier, en Base64. */
    public static void sauvegarderCle(SecretKey cle, Path fichier) throws IOException
    {
        Files.writeString(fichier,
                Base64.getEncoder().encodeToString(cle.getEncoded()));
    }

    /** Relit une clé sauvegardée avec sauvegarderCle. */
    public static SecretKey chargerCle(Path fichier) throws IOException {
        byte[] octets = Base64.getDecoder().decode(Files.readString(fichier).trim());
        return new SecretKeySpec(octets, "AES");
    }

    /** Chiffre un texte et l'écrit dans un fichier texte (Base64). */
    public static void chiffrer(String texte, SecretKey cle, Path fichier)
            throws GeneralSecurityException, IOException {
        byte[] iv = new byte[TAILLE_IV];
        new SecureRandom().nextBytes(iv); // IV aléatoire
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, cle, new GCMParameterSpec(TAILLE_TAG, iv));
        byte[] chiffre = cipher.doFinal(texte.getBytes(StandardCharsets.UTF_8));
        ByteBuffer tampon = ByteBuffer.allocate(iv.length + chiffre.length);
        tampon.put(iv).put(chiffre); // IV d'abord, puis les données
        Files.writeString(fichier,
                Base64.getEncoder().encodeToString(tampon.array()));
    }

    /** Relit un fichier chiffré et renvoie le texte en clair. */
    public static String dechiffrer(SecretKey cle, Path fichier)
            throws GeneralSecurityException, IOException {
        byte[] contenu =  Base64.getDecoder().decode(Files.readString(fichier).trim());
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.DECRYPT_MODE, cle,
                new GCMParameterSpec(TAILLE_TAG, contenu, 0, TAILLE_IV)); // le 12 premiers octets = IV
        byte[] clair = cipher.doFinal(contenu, TAILLE_IV, contenu.length - TAILLE_IV);
        return new String(clair, StandardCharsets.UTF_8);
    }
}
