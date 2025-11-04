package runnity.controller;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

}
