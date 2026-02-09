package demo.controller;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.model.AuthToken;
import demo.model.Credential;
import demo.model.User;
import demo.store.InMemoryStore;
import demo.util.HashUtil;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final InMemoryStore store;

    public AdminController(InMemoryStore store) {
        this.store = store;
    }

    // helper: extract token from header and return AuthToken or null
    private AuthToken getTokenFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String tokenValue = authHeader.substring(7);
        return store.getTokenRepo().get(tokenValue);
    }

    private boolean isAdmin(AuthToken token) {
        if (token == null || token.getUser() == null || token.getUser().getAuthorities() == null) return false;
        return token.getUser().getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getName()));
    }

    // POST /admin/users -> create user (body: {"identifier":"name"})
    @PostMapping("/users")
    public ResponseEntity<Object> createUser(@RequestHeader(value = "Authorization", required = false) String auth,
                                             @RequestBody Map<String, String> body) {
        AuthToken token = getTokenFromAuthHeader(auth);
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!isAdmin(token)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        String identifier = body.get("identifier");
        if (identifier == null || identifier.isBlank()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("identifier required");
        if (store.getUserRepo().containsKey(identifier)) return ResponseEntity.status(HttpStatus.CONFLICT).body("user exists");

        User user = new User();
        user.setUid(UUID.randomUUID().toString());
        user.setIdentifier(identifier);

        store.getUserRepo().put(identifier, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // POST /admin/users/{identifier}/credentials -> add credential {type, secret}
    @PostMapping("/users/{identifier}/credentials")
    public ResponseEntity<Object> addCredential(@RequestHeader(value = "Authorization", required = false) String auth,
                                                @PathVariable String identifier,
                                                @RequestBody Map<String, String> body) {
        AuthToken token = getTokenFromAuthHeader(auth);
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!isAdmin(token)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        User user = store.getUserRepo().get(identifier);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        String type = body.getOrDefault("type", "PASSWORD");
        String secret = body.get("secret");
        if (secret == null || secret.isBlank()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("secret required");

        Credential cred = new Credential();
        cred.setId(UUID.randomUUID().toString());
        cred.setUser(user);
        cred.setType(type);
        cred.setSecretHash(HashUtil.hash(secret));
        cred.setActive(true);

        store.getCredentialRepo().put(identifier, cred);
        return ResponseEntity.status(HttpStatus.CREATED).body(cred);
    }

    // DELETE /admin/users/{identifier} -> delete user and related creds/tokens
    @DeleteMapping("/users/{identifier}")
    public ResponseEntity<Object> deleteUser(@RequestHeader(value = "Authorization", required = false) String auth,
                                             @PathVariable String identifier) {
        AuthToken token = getTokenFromAuthHeader(auth);
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!isAdmin(token)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        User removed = store.getUserRepo().remove(identifier);
        if (removed == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        store.getCredentialRepo().remove(identifier);
        // remove tokens for that user
        var tokensToRemove = store.getTokenRepo().values().stream()
                .filter(t -> t.getUser() != null && identifier.equals(t.getUser().getIdentifier()))
                .map(AuthToken::getValue)
                .collect(Collectors.toList());
        tokensToRemove.forEach(store.getTokenRepo()::remove);

        return ResponseEntity.ok().body("User deleted");
    }
}
