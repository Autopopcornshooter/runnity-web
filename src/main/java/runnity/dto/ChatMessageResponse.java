package runnity.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import runnity.domain.Message;
import runnity.domain.MessageType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageResponse {

    private Long chatRoomId;
    private Long senderId;
    private String senderNickname;
    private String senderProfileUrl;
    private String message;
    private LocalDateTime createdAt;
    private String type;
    private boolean system;

    public static ChatMessageResponse from(Message message) {

        String userProfileUrl = (message.getSenderId().getProfileImage() != null && message.getSenderId().getProfileImage().getUrl() != null)
            ? message.getSenderId().getProfileImage().getUrl() : "/images/runnity-person.png";

        return ChatMessageResponse.builder()
            .chatRoomId(message.getChatRoom().getChatRoomId())
            .senderId(message.getSenderId().getUserId())
            .senderNickname(message.getSenderId().getNickname())
            .senderProfileUrl(userProfileUrl)
            .message(message.getContent())
            .createdAt(message.getCreatedAt())
            .type(message.getType().name())
            .system(message.getType() != MessageType.TEXT)
            .build();
    }

}
