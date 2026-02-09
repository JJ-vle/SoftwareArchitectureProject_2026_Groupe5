package demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import demo.model.Credential;
import demo.model.User;

public interface CredentialRepository extends JpaRepository<Credential, String> {

    Optional<Credential> findByUserAndTypeAndActiveTrue(User user, String type);

}
