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
    private String ownerNickname;
    private List<Long> members;
    private long unreadCount;

    public static ChatRoomResponse from(ChatRoom chatRoom) {
        List<Long> members = chatRoom.getMembers().stream()
            .filter(m -> m.isActive())
            .map(m -> m.getUser().getUserId())
            .toList();

        Long ownerId = (chatRoom.getOwner() != null) ? chatRoom.getOwner().getUserId() : null;
        String ownerNickname = (chatRoom.getOwner() != null) ? chatRoom.getOwner().getNickname() : null;

        return ChatRoomResponse.builder()
            .chatRoomId(chatRoom.getChatRoomId())
            .chatRoomType(chatRoom.getChatRoomType())
            .chatRoomName(chatRoom.getChatRoomName())
            .description(chatRoom.getDescription())
            .region(chatRoom.getRegion())
            .imageUrl(chatRoom.getImageUrl())
            .active(chatRoom.isActive())
            .ownerId(ownerId)
            .ownerNickname(ownerNickname)
            .members(members)
            .unreadCount(0)
            .build();
    }

}
