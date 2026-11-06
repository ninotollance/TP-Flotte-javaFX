package cda.vehicules.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;

public class Utils {

    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static String saisieString(String msg, Scanner scan) {
        while (true) {
            System.out.print(msg);
            String texte = scan.nextLine().trim();
            if (!texte.isEmpty()) {
                return texte;
            }
            System.out.println("La saisie ne peut pas être vide.");
        }
    }

    public static int saisieInt(String msg, Scanner scan) {
        while (true) {
            System.out.print(msg);
            String saisie = scan.nextLine().trim();
            try {
                return Integer.parseInt(saisie);
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre entier valide.");
            }
        }
    }

    public static double saisieDouble(String msg, Scanner scan) {
        while (true) {
            System.out.print(msg);
            String saisie = scan.nextLine().trim().replace(',', '.');
            try {
                return Double.parseDouble(saisie);
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre valide.");
            }
        }
    }

    public static LocalDate saisieDate(String msg, Scanner scan) {
        LocalDate min = LocalDate.of(1950, 1, 1);
        while (true) {
            System.out.print(msg);
            String saisie = scan.nextLine().trim();
            try {
                LocalDate date = LocalDate.parse(saisie, FORMAT_DATE);
                if (date.isAfter(LocalDate.now()) || date.isBefore(min)) {
                    System.out.println("La date doit être comprise entre 01/01/1950 et aujourd'hui.");
                } else {
                    return date;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Format invalide --> jj/mm/aaaa");
            }
        }
    }
}
