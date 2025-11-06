package runnity.controller;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import runnity.domain.User;
import runnity.dto.SignUpRequest;
import runnity.service.UserAuthService;
import runnity.util.CustomSecurityUtil;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class UserAuthController {

  private final UserAuthService userAuthService;
  @Value("${naver.map.client-id}")
  private String naverClientId;

  //테스트용
  @GetMapping("/main")
  public String toMain() {

    return "mainPage";
  }

  //테스트용
  @GetMapping("/test")
  public String testPage() {
    return "test";
  }

  @GetMapping("/signIn")
  public String signInPage() {
    if (CustomSecurityUtil.isAuthenticated()) {
      return "redirect:/main";
    }
    return "signIn";
  }

  @GetMapping("/signUp")
  public String signUpPage(Model model) {
    if (CustomSecurityUtil.isAuthenticated()) {
      return "redirect:/main";
    }
    model.addAttribute("naverClientId", naverClientId);
    model.addAttribute("formData", new SignUpRequest());
    return "signUp";
  }


  @PostMapping("/signIn")
  public void signIn() {
    log.info("WebSecurityConfig.java: signIn(POST)");
  }


  //  로그아웃
  @PostMapping("/signOut")
  public void logout(
  ) {
    log.info("WebSecurityConfig.java: signOut(POST)");
  }

  @PostMapping("/signUp")
  public String signUp(SignUpRequest request, Model model) {

    log.info(request.getNickname());

    if (userAuthService.isLoginIdExist(request.getUsername())) {
      model.addAttribute("formData", request);
      model.addAttribute("loginIdErrorMessage", "이미 존재하는 아이디입니다.");
      return "signUp";
    }
    if (userAuthService.isLoginIdExist(request.getNickname())) {
      model.addAttribute("formData", request);
      model.addAttribute("nickNameErrorMessage", "이미 존재하는 닉네임입니다.");
      return "signUp";
    }

    if (!request.getPassword().equals(request.getPasswordConfirm())) {
      model.addAttribute("formData", request);
      return "signUp";
    }
    User user = userAuthService.saveUserInfo(request);
    if (user != null) {
      log.info("회원가입 성공- loginId:" + user.getLoginId());
    } else {
      log.info("회원가입 실패");
    }

    return "redirect:/api/auth/signIn";
  }


}
