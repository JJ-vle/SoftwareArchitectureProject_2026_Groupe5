package demo.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import demo.model.User;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByIdentifier(String identifier);

}
