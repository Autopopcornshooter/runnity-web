package runnity.service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import runnity.domain.ChatRoom;
import runnity.domain.ChatRoomMember;
import runnity.domain.ChatRoomType;
import runnity.domain.User;
import runnity.domain.UserMatchState;
import runnity.repository.ChatRoomMemberRepository;
import runnity.repository.ChatRoomRepository;
import runnity.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class DuoMatchService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Transactional
    public ChatRoom createRoomAndBind(Long u1, Long u2) {
        if (u1.equals(u2)) {
            throw new IllegalArgumentException("같은 사용자끼리는 매칭할 수 없습니다.");
        }

        Long first = Math.min(u1, u2);
        Long second = Math.max(u1, u2);
        User firstUser = userRepository.findByIdForUpdate(first);
        User secondUser = userRepository.findByIdForUpdate(second);
        User userA = (first.equals(u1)) ? firstUser : secondUser;
        User userB = (second.equals(u2)) ? secondUser : firstUser;

        // 유저 매칭 상태 변경
        userA.setMatchState(UserMatchState.MATCHED);
        userRepository.save(userA);
        userB.setMatchState(UserMatchState.MATCHED);
        userRepository.save(userB);

        // 이미 활성 듀오 참여 여부 검사
        ensureNotInActiveDuo(userA.getUserId());
        ensureNotInActiveDuo(userB.getUserId());

        ChatRoom room = ChatRoom.builder()
            .chatRoomName("랜덤 듀오 채팅")
            .description("자동 생성 듀오 매칭")
            .region(null)
            .imageUrl("/images/runner.png")
            .chatRoomType(ChatRoomType.RANDOM)
            .users(new ArrayList<>())
            .owner(null)
            .build();
        chatRoomRepository.save(room);

        ChatRoomMember m1 = ChatRoomMember.builder()
            .chatRoom(room).user(userA).joinedAt(LocalDateTime.now()).build();
        ChatRoomMember m2 = ChatRoomMember.builder()
            .chatRoom(room).user(userB).joinedAt(LocalDateTime.now()).build();

        room.addMember(m1);
        room.addMember(m2);

        chatRoomMemberRepository.saveAll(List.of(m1, m2));

        return room;
    }

    private void ensureNotInActiveDuo(Long userId) {
        long cnt = chatRoomMemberRepository.countActiveByUserAndType(userId, ChatRoomType.RANDOM);
        if (cnt > 0) {
            throw new IllegalStateException("이미 활성 듀오 채팅방에 참여 중인 사용자입니다. userId=" + userId);
        }
    }

}
