package runnity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import runnity.dto.ChatMessageResponse;
import runnity.service.ChatMessageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatrooms")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @GetMapping("/{chatRoomId}/messages")
    public Page<ChatMessageResponse> getHistoryMessages(@PathVariable Long chatRoomId, Pageable pageable) {
        return chatMessageService.history(chatRoomId, pageable);
    }

}
