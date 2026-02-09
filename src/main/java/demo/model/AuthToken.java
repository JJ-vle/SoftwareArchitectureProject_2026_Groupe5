package demo.model;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
@Table(name = "tokens")
public class AuthToken {

        @Id
    //@GeneratedValue(strategy=GenerationType.AUTO) //@GeneratedValue ne marche pas pour les Strings
    @Column(name = "token_value")
    private String value;          // token
    @PrePersist
    public void prePersist() {
        if (value == null) {
            value = UUID.randomUUID().toString();
        }
    }

    @Column(name = "expiration")
    private Instant expiration;    // date d'expiration

    @OneToOne(mappedBy = "token")
    @JsonBackReference
    private User user;             // utilisateur authentifié

    // constructeurs
    public AuthToken() {}

    public AuthToken(String value, Instant expiration, User user) {
        this.value = value;
        this.expiration = expiration;
        this.user = user;
    }

    // getters/setters
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
