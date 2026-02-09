package demo.init;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import demo.model.Authority;
import demo.model.Credential;
import demo.model.User;
import demo.store.InMemoryStore;
import demo.util.HashUtil;
import jakarta.annotation.PostConstruct;

@Component
public class DataInitializer {

    private final InMemoryStore store;

    public DataInitializer(InMemoryStore store) {
        this.store = store;
    }

    @PostConstruct
    public void init() {
        Authority userRole = new Authority();
        userRole.setName("ROLE_USER");

        User user = new User();
        user.setUid("u1");
        user.setIdentifier("student1");
        user.setAuthorities(Set.of(userRole));

        Credential cred = new Credential();
        cred.setId(UUID.randomUUID().toString());
        cred.setUser(user);
        cred.setType("PASSWORD");
        cred.setSecretHash(HashUtil.hash("password"));
        cred.setActive(true);

        store.getUserRepo().put(user.getIdentifier(), user);
        store.getCredentialRepo().put(user.getIdentifier(), cred);

        // create an initial admin user so admin endpoints can be used
        Authority adminRole = new Authority();
        adminRole.setName("ROLE_ADMIN");

        User admin = new User();
        admin.setUid("a1");
        admin.setIdentifier("admin");
        admin.setAuthorities(Set.of(adminRole));

        Credential adminCred = new Credential();
        adminCred.setId(UUID.randomUUID().toString());
        adminCred.setUser(admin);
        adminCred.setType("PASSWORD");
        adminCred.setSecretHash(HashUtil.hash("adminpass"));
        adminCred.setActive(true);

        store.getUserRepo().put(admin.getIdentifier(), admin);
        store.getCredentialRepo().put(admin.getIdentifier(), adminCred);
    }
}
