package runnity.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import runnity.domain.ChatRoom;
import runnity.domain.ChatRoomType;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findByChatRoomType(ChatRoomType chatRoomType);

}
