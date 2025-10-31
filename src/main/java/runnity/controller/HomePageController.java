package runnity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomePageController {

    @GetMapping("/")
    public String start() {
        return "start";
    }

    @GetMapping("/main")
    public String main() {
        return "main";
    }
}
