package runnity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import runnity.domain.ChatRoom;
import runnity.domain.Friend;
import runnity.domain.FriendLike;
import runnity.dto.FriendInfo;
import runnity.repository.FriendLikeRepository;
import runnity.repository.FriendRepository;
import runnity.service.FriendService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendApiController {
    private final FriendService friendService;
    private final FriendLikeRepository friendLikeRepository;
    private final FriendRepository friendRepository;

    @PostMapping("/add")
    public ResponseEntity<String> addFriend(@RequestBody FriendInfo friendInfo) {
        try {
            friendService.addFriend(friendInfo);
            return ResponseEntity.ok("친구를 추가했습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/searchOnList")
    public List<Friend> searchFriends(@RequestParam String nickname) {
        return friendService.searchByNicknameOnList(nickname);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Integer> increaseLikecount(@PathVariable Long id) {
        Long userId = friendService.getLogInUserID();
        boolean alreadyLiked = friendLikeRepository.existsByUserIdAndFriendId(userId,id);
        if (alreadyLiked) {
            Optional<Friend> friend = friendRepository.findById(id);
            int currentCount = friend.get().getLikecount();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(currentCount);
        }

        friendLikeRepository.save(new FriendLike(userId, id));

        int updatedCount = friendService.increaseLikeCount(id);
        return ResponseEntity.ok(updatedCount);
    }

    @PostMapping("/chat")
    public ResponseEntity<Long> startFriendChat(@RequestBody Map<String, String> requestBody) {
        Long friendId = Long.valueOf(requestBody.get("friendUserId"));
        Long loginUserId = friendService.getLogInUserID();
        ChatRoom room = friendService.FriendChat(loginUserId, friendId);
        return ResponseEntity.ok(room.getChatRoomId()); // 생성된 채팅방 id 반환
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<String> deleteFriend(@PathVariable("id") Long friendId) {
        boolean success = friendService.deleteFriend(friendId);
        if (success) {
            return ResponseEntity.ok("친구를 삭제했습니다.");
        } else {
            return ResponseEntity.badRequest().body("삭제할 친구가 존재하지 않습니다.");
        }
    }
}
