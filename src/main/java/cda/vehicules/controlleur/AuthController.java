package cda.vehicules.controlleur;

import cda.vehicules.service.AuthService;

public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public String connecter(String login, String motDePasse) {
        if (authService.authentifier(login, motDePasse)) {
            return "Connexion réussie !";
        }
        if (authService.estBloque()) {
            return "Identifiants incorrects. Nombre maximal de tentatives atteint ! ";
        }
        return "Identifiants incorrects. Tentatives restantes : " + authService.tentativesRestantes();
    }

    public boolean estBloque() {
        return authService.estBloque();
    }

    public boolean estConnecte() {
        return authService.estConnecte();
    }
}
