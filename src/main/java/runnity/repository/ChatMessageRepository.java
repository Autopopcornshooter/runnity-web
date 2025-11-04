package runnity.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import runnity.domain.Message;

public interface ChatMessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.chatRoom.chatRoomId = :chatRoomId AND m.createdAt >= :from ORDER BY m.messageId DESC")
    Page<Message> findRecentMessages(@Param("chatRoomId") Long chatRoomId, @Param("from") LocalDateTime from, Pageable pageable);

}
