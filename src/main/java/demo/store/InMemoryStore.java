package demo.store;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import demo.model.AuthToken;
import demo.model.Credential;
import demo.model.User;

@Component
public class InMemoryStore {
    private final Map<String, User> userRepo = new HashMap<>();
    private final Map<String, Credential> credentialRepo = new HashMap<>();
    private final Map<String, AuthToken> tokenRepo = new HashMap<>();

    public Map<String, User> getUserRepo() {
        return userRepo;
    }

    public Map<String, Credential> getCredentialRepo() {
        return credentialRepo;
    }

    public Map<String, AuthToken> getTokenRepo() {
        return tokenRepo;
    }
}
