package runnity.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import runnity.dto.ChatRoomRequest;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id", nullable = false, updatable = false)
    private Long chatRoomId;

    @Column(name = "chat_room_name")
    private String chatRoomName;

    @Column(name = "description")
    private String description;

    @Column(name = "region")
    private String region;

    @Column(name = "imageUrl")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_room_type", nullable = false)
    private ChatRoomType chatRoomType;

    // 채팅방 활성상태 체크 확장성을 고려해서 넣어둠
    private boolean active = true;

    // 채팅방 개설자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomMember> members = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void addMember(ChatRoomMember member) {
        this.members.add(member);
    }

    public void deactivate() {
        this.active = false;
    }

    @Builder
    public ChatRoom(String chatRoomName, String description, String region, String imageUrl, ChatRoomType chatRoomType, List<ChatRoomMember> users, User owner) {
        this.chatRoomName = chatRoomName;
        this.description = description;
        this.region = region;
        this.imageUrl = imageUrl;
        this.chatRoomType = chatRoomType;
        this.members = users != null ? users : new ArrayList<>();
        this.owner = owner;
    }

    public void editChatRoom(ChatRoomRequest request, User owner) {
        this.chatRoomName = request.getChatRoomName();
        this.description = request.getDescription();
        this.region = request.getRegion();
        this.imageUrl = request.getImageUrl();
        this.chatRoomType = ChatRoomType.GROUP;
        this.owner = owner;
    }

}
