package runnity.controller;

import java.security.Principal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import runnity.service.ChatRoomService;
import runnity.service.MatchService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match")
public class MatchController {

    private final MatchService matchService;
    private final ChatRoomService chatRoomService;

    @PostMapping("/queue")
    public ResponseEntity<Void> queueMatch(Principal principal) {
        String loginId = principal.getName();
        Long userId = chatRoomService.getCheckUserId(loginId);

        matchService.queueMatch(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/queue")
    public ResponseEntity<Void> deleteMatch(Principal principal) {
        String loginId = principal.getName();
        Long userId = chatRoomService.getCheckUserId(loginId);

        matchService.cancelMatch(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/state")
    public ResponseEntity<Map<String, Object>> state(Principal principal) {
        String loginId = principal.getName();
        Long userId = chatRoomService.getCheckUserId(loginId);

        return ResponseEntity.ok(matchService.state(userId));
    }

}
