package runnity.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import runnity.domain.User;
import runnity.dto.SignUpRequest;
import runnity.service.UserService;
import runnity.util.CustomSecurityUtil;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class UserAuthController {

  private final UserService userService;
  private final PasswordEncoder encoder;
  @Value("${naver.map.client-id}")
  private String naverClientId;

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

  @GetMapping("/re-auth")
  public String reAuthPage() {
    return "reAuth";
  }

  @PostMapping("/re-auth")
  public String reAuthSubmit(@RequestParam("password") String password, HttpSession session
  ) {
    User user = userService.authenticatedUser();

    if (!encoder.matches(password, user.getPassword())) {
      return "redirect:/api/auth/re-auth?error";
    }
    session.setAttribute("reAuth", true);
    return "redirect:/userInfo/update";
  }

  @PostMapping("/signIn")
  public void signInSubmit() {
    log.info("WebSecurityConfig.java: signIn(POST)");
  }


  //  로그아웃
  @PostMapping("/signOut")
  public void logout(
  ) {
    log.info("WebSecurityConfig.java: signOut(POST)");
  }

  @PostMapping("/signUp")
  public String signUpSubmit(SignUpRequest request, Model model) {

    log.info(request.getNickname());

    if (userService.isLoginIdExist(request.getUsername())) {
      model.addAttribute("formData", request);
      model.addAttribute("loginIdErrorMessage", "이미 존재하는 아이디입니다.");
      return "signUp";
    }
    if (userService.isLoginIdExist(request.getNickname())) {
      model.addAttribute("formData", request);
      model.addAttribute("nickNameErrorMessage", "이미 존재하는 닉네임입니다.");
      return "signUp";
    }

    if (!request.getPassword().equals(request.getPasswordConfirm())) {
      model.addAttribute("formData", request);
      return "signUp";
    }
    User user = userService.saveUserInfo(request);
    if (user != null) {
      log.info("회원가입 성공- loginId:" + user.getLoginId());
    } else {
      log.info("회원가입 실패");
    }

    return "redirect:/api/auth/signIn";
  }


}
