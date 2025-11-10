package runnity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import runnity.domain.Friend;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

}
