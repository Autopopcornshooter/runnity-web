package runnity.repository;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import runnity.domain.Friend;
import runnity.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByLoginId(String loginId);

  Optional<User> findByNickname(String nickname);

  boolean existsByLoginId(String loginId);

  boolean existsByNickname(String nickname);

  // 듀오 매칭시스템 관련 추가 => 강준호
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from User u where u.userId = :userId")
  User findByIdForUpdate(@Param("userId") Long userId);

  // 친구 찾기/추가 기능 = > 박주영
  List<User> findByNicknameContainingIgnoreCase(String nickname);
  Optional<User> findById(Long id);

}
