package runnity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import runnity.domain.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

}
