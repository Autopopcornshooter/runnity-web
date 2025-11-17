package runnity.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import runnity.domain.ProfileImage;
import runnity.domain.User;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

  Optional<ProfileImage> findByUser(User user);

  void removeByUser(User user);
}
