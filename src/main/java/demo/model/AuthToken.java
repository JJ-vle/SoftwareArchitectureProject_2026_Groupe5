package demo.model;

import java.time.Instant;

public class AuthToken {

    private String value;          // token
    private Instant expiration;    // date d'expiration
    private User user;             // utilisateur authentifié

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Instant getExpiration() {
        return expiration;
    }

    public void setExpiration(Instant expiration) {
        this.expiration = expiration;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
