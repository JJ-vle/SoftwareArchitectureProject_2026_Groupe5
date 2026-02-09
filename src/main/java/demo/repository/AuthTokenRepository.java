package demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import demo.model.AuthToken;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {
}
