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
            return;
        }

        // ===== AUTHORITIES =====

        Authority roleUser = authorityRepository.save(new Authority("ROLE_USER"));
        Authority roleAdmin = authorityRepository.save(new Authority("ROLE_ADMIN"));

        Authority serviceA = authorityRepository.save(new Authority("SERVICE_A"));
        Authority serviceB = authorityRepository.save(new Authority("SERVICE_B"));

        // ===== USER A (accès service A) =====

        User user1 = new User("student1");
        user1.setAuthorities(Set.of(roleUser, serviceA));
        userRepository.save(user1);

        Credential user1Cred = new Credential(
            UUID.randomUUID().toString(),
            user1,
            "PASSWORD",
            HashUtil.hash("password")
        );

        credentialRepository.save(user1Cred);

        // ===== USER B (accès service B) =====

        User user2 = new User("student2");
        user2.setAuthorities(Set.of(roleUser, serviceB));
        userRepository.save(user2);

        Credential user2Cred = new Credential(
            UUID.randomUUID().toString(),
            user2,
            "PASSWORD",
            HashUtil.hash("password")
        );

        credentialRepository.save(user2Cred);

        // ===== ADMIN (accès A + B + admin) =====

        User admin = new User("admin");
        admin.setAuthorities(Set.of(roleAdmin, serviceA, serviceB));
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
