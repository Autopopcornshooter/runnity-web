package runnity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import runnity.domain.Friend;
import runnity.domain.User;
import runnity.dto.FriendInfo;
import runnity.repository.FriendRepository;
import runnity.service.FriendService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FriendListController {

    private final FriendService friendService;

    @GetMapping("/friendlist")
    public String friendlist(Model model) {
        List<Friend> friends = friendService.findAll();
        model.addAttribute("friends", friends);
        return "friendlist";
    }

    @GetMapping("/friend-search")
    public String openFriendSearchPopup() {
        return "friend-search";
    }

    @ResponseBody
    @GetMapping("/api/friends/search")
    public List<FriendInfo> searchFriends(@RequestParam("nickname") String nickname) {
        return friendService.searchByNickname(nickname);
    }
}
