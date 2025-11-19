package runnity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import runnity.domain.FriendLike;

@Repository
public interface FriendLikeRepository extends JpaRepository<FriendLike, Long> {
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);

}
