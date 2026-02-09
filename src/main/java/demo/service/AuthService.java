package demo.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import demo.model.AuthToken;
import demo.model.Credential;
import demo.repository.AuthTokenRepository;
import demo.repository.CredentialRepository;
import demo.repository.UserRepository;
import demo.model.User;
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

    public AuthService(
        UserRepository userRepository,
        CredentialRepository credentialRepository,
        AuthTokenRepository tokenRepository
    ) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.tokenRepository = tokenRepository;
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

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }    
}
