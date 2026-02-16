package demo.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import demo.model.AuthToken;
import demo.model.Credential;
import demo.repository.AuthTokenRepository;
import demo.repository.VerificationTokenRepository;
import demo.repository.CredentialRepository;
import demo.repository.UserRepository;
import demo.model.User;
import demo.model.VerificationToken;
import demo.model.Credential;
import demo.model.AuthToken;
import demo.util.HashUtil;
import jakarta.transaction.Transactional;


@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final AuthTokenRepository tokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    public AuthService(
        UserRepository userRepository,
        CredentialRepository credentialRepository,
        AuthTokenRepository tokenRepository,
        VerificationTokenRepository verificationTokenRepository
    ) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.tokenRepository = tokenRepository;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    public AuthToken login(String identifier, String password) {
        User user = userRepository.findByIdentifier(identifier)
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        Credential cred = credentialRepository
            .findByUserAndTypeAndActiveTrue(user, "PASSWORD")
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!cred.getSecretHash().equals(HashUtil.hash(password))) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        user.setToken(null); // orphanRemoval

        AuthToken token = new AuthToken();
        token.setExpiration(Instant.now().plus(1, ChronoUnit.HOURS));
        token.setUser(user);

        user.setToken(token);

        return token;
    }

    public void logout(String tokenValue) {
        tokenRepository.deleteById(tokenValue);
    }

    public boolean validate(String tokenValue) {
        AuthToken token = tokenRepository.findById(tokenValue)
            .orElse(null);

        if (token == null) return false;

        if (token.getExpiration().isBefore(Instant.now())) {
            tokenRepository.delete(token);
            return false;
        }
        return true;
    }

    public void register(String email, String password) {

        if (userRepository.findByIdentifier(email).isPresent()) {
            throw new IllegalStateException("user exists");
        }

        User user = new User(email);
        user.setVerified(false);
        userRepository.save(user);

        // password credential
        Credential cred = new Credential();
        cred.setId(UUID.randomUUID().toString());
        cred.setUser(user);
        cred.setType("PASSWORD");
        cred.setSecretHash(HashUtil.hash(password));
        cred.setActive(true);
        credentialRepository.save(cred);

        // verification token
        String tokenClear = UUID.randomUUID().toString();

        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setTokenHash(HashUtil.hash(tokenClear));
        token.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));
        verificationTokenRepository.save(token);

        // PUBLISH EVENT RABBITMQ
    }

    
    
@Transactional
public void verify(String tokenId, String clearToken) {

    // Charger le token par tokenId
    VerificationToken token = verificationTokenRepository
            .findByTokenHash(HashUtil.hash(tokenId))
            .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

    // Vérifier expiration
    if (token.getExpiresAt().isBefore(Instant.now())) {
        verificationTokenRepository.delete(token);
        throw new IllegalArgumentException("Token expired");
    }

    // Charger le user
    User user = userRepository.findById(token.getUser().getUid())
            .orElseThrow(() -> new IllegalStateException("User not found"));

    // supprimer le token
    verificationTokenRepository.delete(token);


    // PUBLISH EMAILVERIFIED
}


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }    
}
