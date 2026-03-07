package demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import demo.model.Authority;

public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
