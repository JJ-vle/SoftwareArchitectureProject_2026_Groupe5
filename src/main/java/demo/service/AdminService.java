package demo.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import demo.model.AuthToken;
import demo.model.Credential;
import demo.model.User;
import demo.repository.AuthTokenRepository;
import demo.repository.CredentialRepository;
import demo.repository.UserRepository;
import demo.util.HashUtil;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final AuthTokenRepository tokenRepository;

    public AdminService(
            UserRepository userRepository,
            CredentialRepository credentialRepository,
            AuthTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.tokenRepository = tokenRepository;
    }

    private AuthToken getToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing token");
        }

        String tokenValue = authHeader.substring(7);

        return tokenRepository.findById(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
    }

    private void checkAdmin(AuthToken token) {
        boolean isAdmin = token.getUser() != null &&
                token.getUser().getAuthorities() != null &&
                token.getUser().getAuthorities()
                        .stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getName()));

        if (!isAdmin) {
            throw new SecurityException("Not admin");
        }
    }

    public User createUser(String authHeader, String identifier) {

        AuthToken token = getToken(authHeader);
        checkAdmin(token);

        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("identifier required");
        }

        if (userRepository.findByIdentifier(identifier).isPresent()) {
            throw new IllegalStateException("user exists");
        }

        User user = new User();
        user.setUid(UUID.randomUUID().toString());
        user.setIdentifier(identifier);

        return userRepository.save(user);
    }

    public Credential addCredential(String authHeader,
                                    String identifier,
                                    String type,
                                    String secret) {

        AuthToken token = getToken(authHeader);
        checkAdmin(token);

        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("secret required");
        }

        Credential cred = new Credential();
        cred.setId(UUID.randomUUID().toString());
        cred.setUser(user);
        cred.setType(type == null ? "PASSWORD" : type);
        cred.setSecretHash(HashUtil.hash(secret));
        cred.setActive(true);

        return credentialRepository.save(cred);
    }

    public void deleteUser(String authHeader, String identifier) {

        AuthToken token = getToken(authHeader);
        checkAdmin(token);

        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        // supprimer credentials
        // PAS BESOIN CAR DELETE ON CASCADE
        /*
        credentialRepository.deleteAll(
                credentialRepository.findByUser(user)
        );
        */

        // supprimer tokens
        // PAS BESOIN CAR DELETE ON CASCADE
        /*
        List<AuthToken> tokens = tokenRepository.findByUser(user);
        tokenRepository.deleteAll(tokens);
        */

        userRepository.delete(user);
    }
}
