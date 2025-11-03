package runnity.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import runnity.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByLoginId(String loginId);

  boolean existsByLoginId(String loginId);

}
