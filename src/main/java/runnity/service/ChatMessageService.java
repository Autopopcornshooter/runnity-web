package runnity.service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import runnity.domain.ChatRoom;
import runnity.domain.ChatRoomMember;
import runnity.domain.Message;
import runnity.domain.MessageType;
import runnity.domain.User;
import runnity.dto.ChatMessageRequest;
import runnity.dto.ChatMessageResponse;
import runnity.repository.ChatMessageRepository;
import runnity.repository.ChatRoomMemberRepository;
import runnity.repository.ChatRoomRepository;
import runnity.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public void send(ChatMessageRequest request) {
        ChatRoom room = chatRoomRepository.findById(request.getChatRoomId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 입니다."));
        User sender = userRepository.findById(request.getSenderId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 입니다."));

        Message message = Message.builder()
            .chatRoom(room)
            .senderId(sender)
            .content(request.getMessage())
            .type(MessageType.TEXT)
            .build();

        Message savedMessage = chatMessageRepository.save(message);

        ChatMessageResponse chatMessageResponse = ChatMessageResponse.from(savedMessage);

        simpMessagingTemplate.convertAndSend(
            "/topic/rooms." + chatMessageResponse.getChatRoomId(),
            chatMessageResponse
        );

        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomId(room.getChatRoomId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 입니다."));

        for (ChatRoomMember member : members) {
            String targetUserId = member.getUser().getLoginId();
            if (targetUserId.equals(sender.getLoginId())) { continue; }

            Map<String, Object> payload = Map.of(
              "type", "NEW_MESSAGE",
              "chatRoomId", room.getChatRoomId(),
                "senderId", sender.getUserId(),
                "message", message.getContent(),
                "messageType", savedMessage.getType().name()
            );

            simpMessagingTemplate.convertAndSendToUser(
                targetUserId,
                "/queue/notify",
                payload
            );
        }

    }

    public Page<ChatMessageResponse> getMessages(Long chatRoomId, Long userId, Pageable pageable) {
        if (chatRoomId == null) throw new IllegalArgumentException("roomId는 필수입니다.");
        if (userId == null) throw new IllegalArgumentException("userId는 필수입니다.");

        // 활성 멤버(현재 세션) 조회
        ChatRoomMember membership = chatRoomMemberRepository
            .findActiveByRoomAndUser(chatRoomId, userId)
            .orElseThrow(() -> new IllegalStateException("현재 채팅방에 참여 중이 아닙니다. 먼저 JOIN 해주세요."));

        LocalDateTime from = membership.getJoinedAt();
        // joinedAt 이후의 메시지만
        Page<Message> page = chatMessageRepository.findRecentMessages(chatRoomId, from, pageable);

        return page.map(m -> ChatMessageResponse.from(m));
    }

    @Transactional
    public long unreadCountForRoom(Long chatRoomId, Long userId) {
        ChatRoomMember m = chatRoomMemberRepository.findActiveMembership(chatRoomId, userId)
            .orElseThrow(() -> new IllegalArgumentException("현재 채팅방에 참여 중이 아닙니다. 먼저 JOIN 해주세요."));
        Long lastId = m.getLastReadMessageId();
        return chatMessageRepository.countUnreadByLastId(chatRoomId, lastId, userId);
    }

    @Transactional
    public void markReadToLastMessage(Long chatRoomId, Long userId) {
        ChatRoomMember member = chatRoomMemberRepository
            .findActiveMembership(chatRoomId, userId)
            .orElseThrow(() -> new IllegalArgumentException("현재 채팅방에 참여 중이 아닙니다. 먼저 JOIN 해주세요."));

        Long lastMessageId = chatMessageRepository.findLastMessageIdByRoom(chatRoomId).orElse(null);
        member.markReadTo(lastMessageId);
    }

    @Transactional
    public Map<Long, Long> unreadCounts(Long userId) {
        Map<Long, Long> result = new HashMap<>();
        for (ChatRoomMember m : chatRoomMemberRepository.findActiveMemberships(userId)) {
            Long lastMessageId = m.getLastReadMessageId();
            long cnt = chatMessageRepository.countUnreadByLastId(m.getChatRoom().getChatRoomId(), lastMessageId, userId);
            result.put(m.getChatRoom().getChatRoomId(), cnt);
        }
        return result;
    }

}
