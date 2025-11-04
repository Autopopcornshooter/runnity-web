package runnity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_room_member",
    indexes = {
        @Index(name="ix_member_active", columnList = "chat_room_id,user_id,active"),
        @Index(name="ix_member_joined", columnList = "chat_room_id,joined_at")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_member_id", nullable = false, updatable = false)
    private Long chatRoomMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Builder
    public ChatRoomMember(ChatRoom chatRoom, User user, LocalDateTime joinedAt) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.joinedAt = joinedAt != null ? joinedAt : LocalDateTime.now();
        this.active = true;
    }

    public void joinGroupChatRoom() {
        this.active = true;
        this.joinedAt = LocalDateTime.now();
        this.leftAt = null;
    }

    public void leaveGroupChatRoom() {
        this.leftAt = LocalDateTime.now();
        this.active = false;
    }

}
