package demo.model;

import java.util.Set;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private String uid;          // identifiant interne

    @Column(name = "identifier")
    private String identifier;   // email / numéro étudiant

    @Column(name = "password")
    private String password;     // credential (simplifié)

    @ManyToMany
    @JoinTable(
        name = "user_auth",                                     // table d’association
        joinColumns = @JoinColumn(name = "user_id"),            // clé étrangère vers User
        inverseJoinColumns = @JoinColumn(name = "authority_id") // clé étrangère vers Authority
    )
    private Set<Authority> authorities;

    @OneToOne
    private AuthToken token;

    // constructeurs
    public User() {}

    public User(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    // gettests/setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }
    
    public AuthToken getToken() {
        return token;
    }

    public void setToken(AuthToken token) {
        this.token = token;
    }
}
