package runnity.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import runnity.domain.ChatRoom;
import runnity.domain.ChatRoomType;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findByChatRoomType(ChatRoomType chatRoomType);

    // 유저 그룹 채팅방 참가여부
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ChatRoom c JOIN c.members m WHERE c.chatRoomId = :chatRoomId AND m.user.userId = :userId")
    boolean existsByIdAndMembers_Id(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);

}
