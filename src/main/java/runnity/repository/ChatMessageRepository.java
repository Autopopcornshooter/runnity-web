package runnity.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import runnity.domain.Message;

public interface ChatMessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.chatRoom.chatRoomId = :chatRoomId AND m.createdAt >= :from ORDER BY m.messageId DESC")
    Page<Message> findRecentMessages(@Param("chatRoomId") Long chatRoomId, @Param("from") LocalDateTime from, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chatRoom.chatRoomId = :chatRoomId AND (:lastId IS NULL OR m.messageId > :lastId) AND m.senderId.userId <> :userId")
    long countUnreadByLastId(@Param("chatRoomId") Long chatRoomId, @Param("lastId") Long lastId, @Param("userId") Long userId);

    @Query("SELECT MAX(m.messageId) FROM Message m WHERE m.chatRoom.chatRoomId = :chatRoomId")
    Optional<Long> findLastMessageIdByRoom(@Param("chatRoomId") Long chatRoomId);

}
