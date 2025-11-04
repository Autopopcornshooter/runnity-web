package runnity.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import runnity.domain.Message;

public interface ChatMessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByChatRoom_ChatRoomIdOrderByMessageIdDesc(Long chatRoomId, Pageable pageable);
    List<Message> findTop50ByChatRoom_ChatRoomIdOrderByMessageIdDesc(Long chatRoomId);
}
