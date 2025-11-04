package runnity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {

    private Long chatRoomId;
    private Long senderId;
    private String message;

}
