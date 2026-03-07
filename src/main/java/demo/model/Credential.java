package demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
@Table(name = "credentials")
public class Credential {

    @Id
    private String id; // simple String id (UUID)

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    private String type; // ex: PASSWORD, API_KEY

    private String secretHash; // hash du credential (ex: mot de passe)

    private boolean active = true;

    public Credential() {}

    public Credential(String id, User user, String type, String secretHash) {
        this.id = id;
        this.user = user;
        this.type = type;
        this.secretHash = secretHash;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSecretHash() {
        return secretHash;
    }

    public void setSecretHash(String secretHash) {
        this.secretHash = secretHash;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
