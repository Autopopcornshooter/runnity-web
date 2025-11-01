package runnity.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import runnity.domain.ChatRoomType;

@Getter
@Setter
public class ChatRoomRequest {

    private Long ownerId;
    private String chatRoomName;
    private String description;
    private String region;
    private String imageUrl;
    private ChatRoomType chatRoomType;
    private List<Long> members;

}
