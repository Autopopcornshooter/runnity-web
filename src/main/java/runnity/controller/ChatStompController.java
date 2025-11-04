package runnity.controller;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import runnity.dto.ChatMessageRequest;
import runnity.service.ChatMessageService;
import runnity.service.ChatRoomService;

@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageRequest request, Principal principal) {
        // 어차피 JS 에서 userId 를 가져올 때 로그인한 사용자의 userId 를 가져와서 굳이 필요 없을 듯.
        // String username = principal.getName();
        // Long senderId = chatRoomService.getCheckUserId(username);
        // request.setSenderId(senderId);
        chatMessageService.send(request);
    }

}
