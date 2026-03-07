package demo.init;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import demo.model.Authority;
import demo.model.Credential;
import demo.model.User;
import demo.repository.AuthorityRepository;
import demo.repository.CredentialRepository;
import demo.repository.UserRepository;
import demo.util.HashUtil;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

@Component
@Transactional
public class DataInitializer {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final AuthorityRepository authorityRepository;

    public DataInitializer(
        UserRepository userRepository,
        CredentialRepository credentialRepository,
        AuthorityRepository authorityRepository
    ) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.authorityRepository = authorityRepository;
    }

    @PostConstruct
    public void init() {

        if (userRepository.count() > 0) {
            return; // évite les doublons au restart
        }

        // ===== ROLE_USER =====
        Authority userRole = new Authority("ROLE_USER");
        authorityRepository.save(userRole);

        User user = new User("student1");
        user.setAuthorities(Set.of(userRole));
        userRepository.save(user);

        Credential userCred = new Credential(
            UUID.randomUUID().toString(),
            user,
            "PASSWORD",
            HashUtil.hash("password")
        );
        credentialRepository.save(userCred);

        // ===== ROLE_ADMIN =====
        Authority adminRole = new Authority("ROLE_ADMIN");
        authorityRepository.save(adminRole);

        User admin = new User("admin");
        admin.setAuthorities(Set.of(adminRole));
        userRepository.save(admin);

        Credential adminCred = new Credential(
            UUID.randomUUID().toString(),
            admin,
            "PASSWORD",
            HashUtil.hash("adminpass")
        );
        credentialRepository.save(adminCred);
    }
}
