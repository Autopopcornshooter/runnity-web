package runnity.controller;

import java.security.Principal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import runnity.dto.ChatMessageResponse;
import runnity.service.ChatMessageService;
import runnity.service.ChatRoomService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;

    @GetMapping("/{chatRoomId}/messages")
    public Page<ChatMessageResponse> getHistoryMessages(@PathVariable Long chatRoomId, Pageable pageable, Principal principal) {
        String loginId = principal.getName();
        Long userId = chatRoomService.getCheckUserId(loginId);
        return chatMessageService.getMessages(chatRoomId, userId, pageable);
    }

    @PutMapping("/{chatRoomId}/read")
    public ResponseEntity<Void> markRoomRead(@PathVariable Long chatRoomId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId =  chatRoomService.getCheckUserId(principal.getName());
        chatMessageService.markReadToLastMessage(chatRoomId, userId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread-counts")
    public ResponseEntity<Map<Long, Long>> getUnreadCounts(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = chatRoomService.getCheckUserId(principal.getName());
        Map<Long, Long> unreadCounts = chatMessageService.unreadCounts(userId);

        return ResponseEntity.ok(unreadCounts);
    }

}
