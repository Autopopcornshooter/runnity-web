package runnity.service;

import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import runnity.domain.ChatRoom;
import runnity.domain.Message;
import runnity.domain.User;
import runnity.dto.ChatMessageRequest;
import runnity.dto.ChatMessageResponse;
import runnity.repository.ChatMessageRepository;
import runnity.repository.ChatRoomRepository;
import runnity.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
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
            .build();

        Message savedMessage = chatMessageRepository.save(message);

        ChatMessageResponse chatMessageResponse = ChatMessageResponse.from(savedMessage);

        simpMessagingTemplate.convertAndSend(
            "/topic/rooms." + chatMessageResponse.getChatRoomId(),
            chatMessageResponse
        );

    }

    @Transactional
    public Page<ChatMessageResponse> history(Long chatRoomId, Pageable pageable) {
        return chatMessageRepository
            .findByChatRoom_ChatRoomIdOrderByMessageIdDesc(chatRoomId, pageable)
            .map(chat -> ChatMessageResponse.from(chat));
    }

    @Transactional
    public List<ChatMessageResponse> recentMessages(Long chatRoomId, int limit) {
        return chatMessageRepository
            .findTop50ByChatRoom_ChatRoomIdOrderByMessageIdDesc(chatRoomId)
            .stream().limit(limit)
            .map(chat -> ChatMessageResponse.from(chat))
            .toList();
    }
}
