package runnity.repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import runnity.domain.ChatRoom;
import runnity.domain.ChatRoomMember;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    // 소속되어있는 채팅방 불러오기
    @Query("SELECT m.chatRoom FROM ChatRoomMember m WHERE m.user.userId = :userId")
    Optional<List<ChatRoom>> findChatRoomsByUserId(@Param("userId") Long userId);

    // 채팅방 멤버 수
    @Query("SELECT COUNT(m) FROM ChatRoomMember m WHERE m.chatRoom.chatRoomId = :roomId")
    int countActiveMembersByChatRoomId(@Param("roomId") Long roomId);

    // 채팅방 나가기
    @Transactional
    @Modifying
    @Query("DELETE FROM ChatRoomMember m WHERE m.chatRoom.chatRoomId = :roomId AND m.user.userId = :userId")
    void deleteByChatRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    // 채팅방 폭파? 이건 언제 사용할지 모르겠지만 일단 만들어 놓음.
    @Modifying
    @Query("DELETE FROM ChatRoom m WHERE m.chatRoomId = :roomId")
    void deleteAllByChatRoomId(@Param("roomId") Long roomId);

}
