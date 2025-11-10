package runnity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FriendListController {

    @GetMapping("/friendlist")
    public String friendlist() {
        return "friendlist";
    }

//    @GetMapping("/moveToMain")
//    public String moveToMain() {
//        return "main";
//    }
//
//    @GetMapping("/moveToChatList")
//    public String moveToChatList() {
//        return "chat-room-list";
//    }
//
//    @GetMapping("/movetToMyChat")
//    public String movetToMyChat() {
//        return "group-chat-list";
//    }
}
