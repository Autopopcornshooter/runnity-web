package runnity.controller;

import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import runnity.dto.ChatRoomResponse;
import runnity.service.ChatRoomService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat-room")
public class ChatRoomViewController {

    private final ChatRoomService chatRoomService;

    @GetMapping("/list")
    public String chatList(Model model) {

        List<ChatRoomResponse> list = chatRoomService.getAllChatRoom();

        model.addAttribute("chatRooms", list);

        return "chat/group-chat-list";
    }

    @GetMapping("/my-chat-list")
    public String myChatList(Model model, Principal principal) {
        if (principal == null) return "redirect:/api/auth/signIn";
        String loginId = principal.getName();
        Long userId = chatRoomService.getCheckUserId(loginId);

        List<ChatRoomResponse> list = chatRoomService.getMyChatRoom(userId);

        model.addAttribute("currentUserId", userId);
        model.addAttribute("chatRooms", list);

        return "chat/my-chat-list";
    }

    @GetMapping("/my-chat-list/{chatRoomId}")
    public String openRoom(@PathVariable Long chatRoomId, Principal principal, Model model) {
        if (principal == null) return "redirect:/api/auth/signIn";
        String loginId = principal.getName();
        Long userId = chatRoomService.getCheckUserId(loginId);

        List<ChatRoomResponse> myRooms = chatRoomService.getMyChatRoom(userId);

        model.addAttribute("chatRooms", myRooms);
        model.addAttribute("currentUserId", userId);
        return "chat/my-chat-list"; // 같은 화면 재사용
    }

    // 채팅방 생성 페이지
    @GetMapping("/create")
    public String createChatRoomForm(Model model, Principal principal) {
        if (principal == null) return "redirect:/api/auth/signIn";
        model.addAttribute("isEdit", false);
        model.addAttribute("chatRoom", new ChatRoomResponse()); // 빈 객체 전달
        return "chat/chat-room-form";
    }

    // 채팅방 수정 페이지
    @GetMapping("/edit/{chatRoomId}")
    public String editChatRoomForm(@PathVariable Long chatRoomId, Model model, Principal principal) {
        ChatRoomResponse chatRoom = chatRoomService.findById(chatRoomId);

        // 로그인한 유저가 만든 방인지 확인
        String loginId = chatRoomService.getLoginId(chatRoom.getOwnerId());
        boolean isOwner = loginId.equals(principal.getName());

        model.addAttribute("isEdit", true);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("chatRoom", chatRoom);

        return "chat/chat-room-form";
    }

}
