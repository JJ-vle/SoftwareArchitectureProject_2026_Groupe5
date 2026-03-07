package demo.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "verification_tokens")
public class VerificationToken {

    @Id
    private String tokenId;
    @PrePersist
    public void prePersist() {
        if (tokenId == null) {
            tokenId = UUID.randomUUID().toString();
        }
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String tokenHash;

    private Instant expiresAt;

    public VerificationToken() {}

    // getters/setters
    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
    public String getTokenId() {
        return tokenId;
    }

    public void setUser(User user) {
        this.user = user;
    }
    public User getUser() {
        return user;
    } 

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }
    public String getTokenHash() {
        return tokenHash;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }
}

