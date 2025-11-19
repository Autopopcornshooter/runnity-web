package runnity.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import runnity.domain.*;
import runnity.dto.FriendInfo;
import runnity.repository.ChatRoomMemberRepository;
import runnity.repository.ChatRoomRepository;
import runnity.repository.FriendRepository;
import runnity.repository.UserRepository;
import runnity.util.CustomSecurityUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    public List<Friend> findAll() {
        return friendRepository.findAll();
    }

    public List<FriendInfo> searchByNickname(String nickname) {
        List<User> users = userRepository.findByNicknameContainingIgnoreCase(nickname);
        List<FriendInfo> friends = users.stream()
                .map(user -> new FriendInfo(
                        user.getUserId(),
                        user.getNickname(),
                        user.getRunnerLevel().name(),
                        user.getRegion() != null ? user.getRegion().getAddress() : "",
                        user.getLikecount()
                ))
                        .toList();
        return friends;
    }

    public boolean addFriend(FriendInfo friendInfo) {
        // ✅ 이미 추가된 친구인지 확인
        List<Friend> existingFriendList = friendRepository.findByUserId(friendInfo.getUserId());
        boolean alreadyFriend = existingFriendList.stream()
                .anyMatch(f -> friendInfo.getUserId().equals(f.getUserId())); // targetUserId를 기준으로 equals 호출

        if (alreadyFriend) {
            throw new IllegalStateException("이미 추가된 친구입니다.");
        }

        Friend friend = new Friend(
                friendInfo.getUserId(),
                friendInfo.getNickname(),
                friendInfo.getRunner_level(),
                friendInfo.getAddress(),
                friendInfo.getLikecount());

        friendRepository.save(friend);
        return true;
    }

    public List<Friend> searchByNicknameOnList(String nickname) {
        return friendRepository.findByNicknameContainingIgnoreCase(nickname);
    }

    @Transactional
    public int increaseLikeCount(Long friendId) {
        Friend friend = friendRepository.findById(friendId)
                .orElseThrow(() -> new IllegalArgumentException("해당 친구가 존재하지 않습니다. id=" + friendId));

        friend.setLikecount(friend.getLikecount() + 1);
        friendRepository.save(friend);
        return friend.getLikecount();
    }

    public boolean deleteFriend(Long friendId) {
        Optional<Friend> friendOpt = friendRepository.findById(friendId);
        if (friendOpt.isEmpty()) {
            return false; // 친구가 존재하지 않음
        }
        friendRepository.deleteById(friendId);
        return true;
    }

    public Long getLogInUserID() {
        String loginId = CustomSecurityUtil.getCurrentUserLoginId();
        Long userId = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("유저 없음"))
                .getUserId();

        return userId;
    }

    @Transactional
    public ChatRoom FriendChat(Long u1, Long u2) {
        if (u1.equals(u2)) {
            throw new IllegalArgumentException("같은 사용자끼리는 매칭할 수 없습니다.");
        }

        Long first = Math.min(u1, u2);
        Long second = Math.max(u1, u2);
        User firstUser = userRepository.findByIdForUpdate(first);
        User secondUser = userRepository.findByIdForUpdate(second);
        User userA = (first.equals(u1)) ? firstUser : secondUser;
        User userB = (second.equals(u2)) ? secondUser : firstUser;

        String nickname = userRepository.findById(u2)
                .map(User::getNickname)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        ChatRoom room = ChatRoom.builder()
                .chatRoomName(nickname + "님과의 채팅")
                .description("친구와 1대1 채팅")
                .region(null)
                .imageUrl("/images/runner.png")
                .chatRoomType(ChatRoomType.DIRECT)
                .users(new ArrayList<>())
                .owner(null)
                .build();
        chatRoomRepository.save(room);

        ChatRoomMember m1 = ChatRoomMember.builder()
                .chatRoom(room).user(userA).joinedAt(LocalDateTime.now()).build();
        ChatRoomMember m2 = ChatRoomMember.builder()
                .chatRoom(room).user(userB).joinedAt(LocalDateTime.now()).build();

        room.addMember(m1);
        room.addMember(m2);

        chatRoomMemberRepository.saveAll(List.of(m1, m2));

        return room;
    }

}
