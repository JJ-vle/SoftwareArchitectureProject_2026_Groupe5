package demo.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import demo.model.Authority;
import demo.model.User;

@RestController
@RequestMapping("/admin")
public class AdminRestController {

    // accès repos existants
    private static Map<String, User> userRepo = AuthRestController.userRepo;

    /**
     * GET /admin/users
     * Liste tous les utilisateurs
     */
    @GetMapping("/users")
    public ResponseEntity<Object> listUsers() {
        return new ResponseEntity<>(userRepo.values(), HttpStatus.OK);
    }

    /**
     * POST /admin/users
     * Ajoute un nouvel utilisateur
     */
    @PostMapping("/users")
    public ResponseEntity<Object> addUser(@RequestBody User user) {

        if (userRepo.containsKey(user.getIdentifier())) {
            return new ResponseEntity<>("User already exists", HttpStatus.CONFLICT);
        }

        userRepo.put(user.getIdentifier(), user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    /**
     * PUT /admin/users/{identifier}/authorities
     * Modifie les authorities
     */
    @PutMapping("/users/{identifier}/authorities")
    public ResponseEntity<Object> updateAuthorities(
            @PathVariable String identifier,
            @RequestBody Set<Authority> authorities) {

        User user = userRepo.get(identifier);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        user.setAuthorities(authorities);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * DELETE /admin/users/{identifier}
     * Supprime un utilisateur
     */
    @DeleteMapping("/users/{identifier}")
    public ResponseEntity<Object> deleteUser(@PathVariable String identifier) {

        User removed = userRepo.remove(identifier);
        if (removed == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>("User deleted", HttpStatus.OK);
    }
}
