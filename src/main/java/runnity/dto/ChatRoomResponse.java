package runnity.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import runnity.domain.ChatRoom;
import runnity.domain.ChatRoomType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomResponse {

    private Long chatRoomId;
    private ChatRoomType chatRoomType;
    private String chatRoomName;
    private String description;
    private String region;
    private String imageUrl;
    private boolean active;
    private Long ownerId;
    private List<Long> members;

    public static ChatRoomResponse from(ChatRoom chatRoom) {
        List<Long> members = chatRoom.getMembers().stream()
            .map(m -> m.getUser().getUserId())
            .toList();

        return ChatRoomResponse.builder()
            .chatRoomId(chatRoom.getChatRoomId())
            .chatRoomType(chatRoom.getChatRoomType())
            .chatRoomName(chatRoom.getChatRoomName())
            .description(chatRoom.getDescription())
            .region(chatRoom.getRegion())
            .imageUrl(chatRoom.getImageUrl())
            .active(chatRoom.isActive())
            .ownerId(chatRoom.getOwner().getUserId())
            .members(members)
            .build();
    }

}
