package runnity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import runnity.domain.Friend;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findByUserId(Long userId);
    // 키워드 일부만 탐색 가능, 대소문자 구분 X
    List<Friend> findByNicknameContainingIgnoreCase(String nickname);
    Optional<Friend> findById(Long Id);
}
