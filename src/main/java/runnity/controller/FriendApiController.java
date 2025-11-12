package runnity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import runnity.domain.Friend;
import runnity.dto.FriendInfo;
import runnity.service.FriendService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendApiController {
    private final FriendService friendService;

    @GetMapping("/ping")
    public String ping() {
        System.out.println("test");
        return "컨트롤러 스캔됨!";
    }

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
}
