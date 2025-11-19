package runnity.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "friend_like")
public class FriendLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;     // 좋아요 누른 사람
    private Long friendId;   // 대상 친구 id

    public FriendLike() {}

    public FriendLike(Long userId, Long friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }
}
