package demo.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import demo.model.AuthToken;
import demo.model.Credential;
import demo.model.User;
import demo.store.InMemoryStore;
import demo.util.HashUtil;

@RestController
@RequestMapping("/auth")
public class AuthRestController {

    private final InMemoryStore store;

    public AuthRestController(InMemoryStore store) {
        this.store = store;
    }

    // LOGIN
    /**
     * POST /auth/login
     * Authentifie un utilisateur avec son identifiant et son mot de passe
     * @param credentials = JSON map avec keys "identifier" et "password"
     * @return AuthToken si succès, 401 si échec
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<AuthToken> login(@RequestBody Map<String, String> credentials) {

        String identifier = credentials.get("identifier");
        String password = credentials.get("password");

        // recup utilisateur depuis repo
        User user = store.getUserRepo().get(identifier);
        Credential cred = store.getCredentialRepo().get(identifier);

        // verif connexion
        if (user == null || cred == null || !cred.isActive() || !cred.getSecretHash().equals(HashUtil.hash(password))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // cree token
        AuthToken token = new AuthToken();
        token.setValue(UUID.randomUUID().toString());
        token.setExpiration(Instant.now().plus(1, ChronoUnit.HOURS)); //1h
        token.setUser(user);

        // sauvegarde et renvoie token
        store.getTokenRepo().put(token.getValue(), token);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    // LOGOUT
    @RequestMapping(value = "/logout/{token}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> logout(@PathVariable String token) {
        store.getTokenRepo().remove(token);
        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }

    // LIST USERS (debug)
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public ResponseEntity<Object> getUsers() {
        return new ResponseEntity<>(store.getUserRepo().values(), HttpStatus.OK);
    }

    // VALIDATE TOKEN
    @RequestMapping(value = "/validate/{token}", method = RequestMethod.GET)
    public ResponseEntity<Object> validateToken(@PathVariable String token) {
        AuthToken t = store.getTokenRepo().get(token);
        if (t == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Instant exp = t.getExpiration();
        if (exp != null && exp.isBefore(Instant.now())) {
            store.getTokenRepo().remove(token);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired");
        }
        return ResponseEntity.ok().body("Token valid");
    }

}
