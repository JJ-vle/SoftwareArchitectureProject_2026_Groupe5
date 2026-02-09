package demo.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
@Table(name="authorities")
public class Authority {

    @Id
    private String name; // ROLE_USER, ROLE_ADMIN

    @ManyToMany(mappedBy = "authorities")
    @JsonBackReference
    private Set<User> users;

    // constructeurs
    public Authority() {}

    public Authority(String name) {
        this.name = name;
    }

    // getters/setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}
