package runnity.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import runnity.domain.Message;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageResponse {

    private Long chatRoomId;
    private Long senderId;
    private String message;
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(Message message) {
        return ChatMessageResponse.builder()
            .chatRoomId(message.getChatRoom().getChatRoomId())
            .senderId(message.getSenderId().getUserId())
            .message(message.getContent())
            .createdAt(message.getCreatedAt())
            .build();
    }

}
