package runnity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import runnity.domain.Friend;
import runnity.domain.User;
import runnity.dto.FriendInfo;
import runnity.repository.FriendRepository;
import runnity.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    public List<Friend> findAll() {
        return friendRepository.findAll();
    }

    public List<FriendInfo> searchByNickname(String nickname) {
        List<User> users = userRepository.findByNickname(nickname);
        List<FriendInfo> friends = users.stream()
                .map(user -> new FriendInfo(user.getUserId(), user.getNickname(), user.getRunnerLevel().name(), user.getRegion().getAddress()))
                        .toList();
        return friends;
    }

    public boolean addFriend(FriendInfo friendInfo) {
        // ✅ 이미 추가된 친구인지 확인
        boolean exists = friendRepository.findAll().stream()
                .anyMatch(f -> f.getUserId().equals(friendInfo.getUserId()));

        if (exists) return false; // 중복된 친구

        Friend friend = new Friend(
                null,
                friendInfo.getUserId(),
                friendInfo.getNickname(),
                friendInfo.getRunner_level(),
                friendInfo.getAddress(),
                0);
//        friend.setUserId(friendInfo.getUserId());
//        friend.setNickname(friendInfo.getNickname());
//        friend.setRunner_level(friendInfo.getRunner_level());
//        friend.setAddress(friendInfo.getAddress());
        friendRepository.save(friend);
        return true;
    }
}
