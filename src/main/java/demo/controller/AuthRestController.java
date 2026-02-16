package demo.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
import demo.service.AuthService;
import demo.store.InMemoryStore;
import demo.util.HashUtil;

@RestController
@RequestMapping("/auth")
public class AuthRestController {

    private final AuthService authService;

    public AuthRestController(AuthService authService) {
        this.authService = authService;
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
    
        try {
            AuthToken token = authService.login(
                credentials.get("identifier"),
                credentials.get("password")
            );
            return ResponseEntity.ok(token);
    
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }    

    // LOGOUT
    @RequestMapping(value = "/logout/{token}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> logout(@PathVariable String token) {
        authService.logout(token);
        return ResponseEntity.ok("Logged out successfully");
    }
    
    // LIST USERS (debug)
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }    

    // VALIDATE TOKEN
    @RequestMapping(value = "/validate/{token}", method = RequestMethod.GET)
    public ResponseEntity<Object> validateToken(@PathVariable String token) {
    
        boolean valid = authService.validate(token);
    
        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok("Token valid");
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<Object> register(@RequestBody Map<String, String> body) {

        try {
            authService.register(
                body.get("email"),
                body.get("password")
            );

            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    

}
