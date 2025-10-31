package runnity.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import runnity.dto.SignUpRequest;
import runnity.service.UserAuthService;

@Controller
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserAuthController {

  private final UserAuthService userAuthService;

  @GetMapping("/signIn")
  public String signInPage() {
    return "";
  }

  @GetMapping("/signUp")
  public String signUpPage() {
    return "";
  }

  @PostMapping("/signIn")
  public void signIn() {
    throw new IllegalStateException("Spring Security 에서 로그인 수행");
  }

  @PostMapping("/signOut")
  public void signOut() {
    throw new IllegalStateException("Spring Security 에서 로그아웃 수행");
  }

  @PostMapping("/signUp")
  public void signUp(SignUpRequest request, Model model) {
    userAuthService.saveUserInfo(request);
  }


}
