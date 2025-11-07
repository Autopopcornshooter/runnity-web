package runnity.controller;

import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import runnity.domain.Region;
import runnity.domain.User;
import runnity.dto.ChatRoomResponse;
import runnity.exceptions.UserNotFoundException;
import runnity.repository.UserRepository;
import runnity.service.ChatRoomService;
import runnity.service.UserAuthService;
import runnity.util.CustomSecurityUtil;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat-room")
public class ChatRoomViewController {

    private final ChatRoomService chatRoomService;
    private final UserAuthService userAuthService;

    @GetMapping("/list")
    public String chatList(Model model) {

        List<ChatRoomResponse> list = chatRoomService.getAllGroupChatRoom();

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
    public String createChatRoomForm(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/api/auth/signIn";

        User user = userAuthService.authenticatedUser();
        Region region = user.getRegion();
        String chatRegion = (region != null) ? region.getAddress() : null;

        if (chatRegion == null || chatRegion.isBlank()) {
            redirectAttributes.addFlashAttribute("alert", "채팅방을 만들기 전에 먼저 지역을 설정해주세요.");
            // 여기는 나중에 지역설정하는 페이지로 변경 예정
            return "redirect:/chat-room/list";
        }

        model.addAttribute("chatRegion", chatRegion);
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
