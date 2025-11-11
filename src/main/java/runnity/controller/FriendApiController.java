package runnity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import runnity.dto.FriendInfo;
import runnity.service.FriendService;

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
        System.out.println("test");
        boolean added = friendService.addFriend(friendInfo);
        if (added) {
            return ResponseEntity.ok("친구가 추가되었습니다.");
        } else {
            return ResponseEntity.status(409).body("이미 추가된 친구입니다."); // 409 Conflict
        }
    }
}
