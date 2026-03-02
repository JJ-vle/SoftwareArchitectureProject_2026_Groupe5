package demo.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import demo.event.EmailVerifiedEvent;
import demo.event.UserRegisteredEvent;
import demo.model.AuthToken;
import demo.model.Credential;
import demo.repository.AuthTokenRepository;
import demo.repository.VerificationTokenRepository;
import demo.repository.CredentialRepository;
import demo.repository.UserRepository;
import demo.model.User;
import demo.model.VerificationToken;
import demo.util.HashUtil;
import jakarta.transaction.Transactional;


@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final AuthTokenRepository tokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    private final RabbitTemplate rabbitTemplate;

    public AuthService(
        UserRepository userRepository,
        CredentialRepository credentialRepository,
        AuthTokenRepository tokenRepository,
        VerificationTokenRepository verificationTokenRepository,
        RabbitTemplate rabbitTemplate
    ) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.tokenRepository = tokenRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.rabbitTemplate = rabbitTemplate;
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
        String tokenId = UUID.randomUUID().toString();
        String tokenClear = UUID.randomUUID().toString();

        VerificationToken token = new VerificationToken();
        token.setTokenId(tokenId);
        token.setUser(user);
        token.setTokenHash(HashUtil.hash(tokenClear));
        token.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));
        verificationTokenRepository.save(token);

        // PUBLISH EVENT RABBITMQ
        UserRegisteredEvent event = new UserRegisteredEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                user.getUid(),
                user.getIdentifier(),
                tokenId,
                tokenClear
        );

        rabbitTemplate.convertAndSend(
                "auth.events",
                "auth.user-registered",
                event
        );

    }
        
    @Transactional
    public void verify(String tokenId, String clearToken) {

        VerificationToken token = verificationTokenRepository
                .findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        // expiration
        if (token.getExpiresAt().isBefore(Instant.now())) {
            verificationTokenRepository.delete(token);
            throw new IllegalArgumentException("Token expired");
        }

        // comparer le secret
        if (!token.getTokenHash().equals(HashUtil.hash(clearToken))) {
            throw new IllegalArgumentException("Invalid token");
        }

        User user = token.getUser();

        // idempotence
        if (!user.isVerified()) {
            user.setVerified(true);
        }

        // one-shot
        verificationTokenRepository.delete(token);

        // publish EmailVerified event
        EmailVerifiedEvent event = new EmailVerifiedEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                user.getUid()
        );

        rabbitTemplate.convertAndSend(
                "auth.events",
                "auth.email-verified",
                event
        );
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }    
}
