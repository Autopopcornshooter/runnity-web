package runnity.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import runnity.domain.User;
import runnity.dto.ChangeRegionRequest;
import runnity.dto.SignUpRequest;
import runnity.dto.UpdateUserInfoRequest;
import runnity.service.RegionService;
import runnity.service.UserService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/userInfo")
@Slf4j
public class UserInfoController {

  private final RegionService regionService;
  private final UserService userService;

  @Value("${naver.map.client-id}")
  private String naverClientId;

  @GetMapping("/region")
  public String checkRegionPage(Model model) {
    model.addAttribute("naverClientId", naverClientId);
    return "checkRegion";
  }

  @GetMapping("/update")
  public String updateUserInfoPage(Model model) {
    //TODO 비밀번호 입력해야 view 이동 조건
    log.info("회원정보 수정 페이지 이동");
    model.addAttribute("formData", new SignUpRequest());
    return "setUserProfile";
  }

  @PostMapping("/region")
  public String changeRegion(ChangeRegionRequest request) {
    regionService.changeRegion(request);
    log.info("지역 정보 변경 : {}",
        request.getAddress() + " " + request.getLat().toString() + " " + request.getLng()
            .toString());
    return "redirect:/main";
  }

  @PostMapping("/update")
  public String updateUserInfo(UpdateUserInfoRequest request, Model model) {
    User user = userService.authenticatedUser();

    if (!request.getLoginId().equals(user.getLoginId())
        && userService.isLoginIdExist(
        request.getLoginId())) {
      model.addAttribute("formData", request);
      model.addAttribute("loginIdErrorMessage", "이미 존재하는 아이디입니다.");
      return "setUserProfile";
    }

    if (!request.getNickName().equals(user.getNickname())
        && userService.isNicknameExist(request.getNickName())) {
      model.addAttribute("formData", request);
      model.addAttribute("nickNameErrorMessage", "이미 존재하는 닉네임입니다.");
      return "setUserProfile";
    }
    if (!request.getPassword().equals(request.getPasswordConfirm())) {
      model.addAttribute("formData", request);
      return "setUserProfile";
    }
    userService.updateUserInfo(request);
    return "redirect:/main";
  }


}
