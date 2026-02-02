package demo.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import demo.model.AuthToken;
import demo.model.Authority;
import demo.model.User;

@RestController
@RequestMapping("/auth")
public class AuthRestController {

    // stockage temporaire utilisateurs et tokens
    private static Map<String, User> userRepo = new HashMap<>();
    private static Map<String, AuthToken> tokenRepo = new HashMap<>();

    // donnees de base pour test
    static {
        Authority userRole = new Authority();
        userRole.setName("ROLE_USER");

        User user = new User();
        user.setUid("u1");
        user.setIdentifier("student1");
        user.setPassword("password");
        user.setAuthorities(Set.of(userRole));

        userRepo.put(user.getIdentifier(), user);
    }

    // LOGIN
    /**
     * POST /auth/login
     * Authentifie un utilisateur avec son identifiant et son mot de passe
     * @param credentials = objet User avec identifier + password envoyés par le client
     * @return AuthToken si succès, 401 si échec
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<AuthToken> login(@RequestBody User credentials) {
    
        // recup utilisation depuis repo
        User user = userRepo.get(credentials.getIdentifier());
    
        // verif connexion
        if (user == null || !user.getPassword().equals(credentials.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    
        // cree token
        AuthToken token = new AuthToken();
        token.setValue(UUID.randomUUID().toString());
        token.setExpiration(Instant.now().plus(1, ChronoUnit.HOURS)); //1h
        token.setUser(user);
    
        // sauvegarde et renvoie token
        tokenRepo.put(token.getValue(), token);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    // LOGOUT
    /**
     * DELETE /auth/logout/{token}
     * Supprime un token d'authentification existant
     * @param token = valeur du token à invalider
     * @return confirmation de logout
     */
    @RequestMapping(value = "/logout/{token}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> logout(@PathVariable String token) {
        // suppr token du repo
        tokenRepo.remove(token);
        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }

    // LIST USERS (debug)
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public ResponseEntity<Object> getUsers() {
        return new ResponseEntity<>(userRepo.values(), HttpStatus.OK);
    }
    
}
