package runnity.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import runnity.domain.Friend;
import runnity.domain.User;
import runnity.dto.FriendInfo;
import runnity.repository.FriendRepository;
import runnity.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

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
}
