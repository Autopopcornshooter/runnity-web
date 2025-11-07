package runnity.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import runnity.domain.Region;
import runnity.domain.User;
import runnity.dto.ChangeRegionRequest;
import runnity.dto.RunnerLevelRequest;
import runnity.dto.SignUpRequest;
import runnity.dto.UpdateUserInfoRequest;
import runnity.service.RegionService;
import runnity.service.UserService;
import runnity.util.CustomSecurityUtil;

@Controller
@RequiredArgsConstructor
@RequestMapping("/userInfo")
@Slf4j
public class UserInfoController {

  private final RegionService regionService;
  private final UserService userService;

  @Value("${naver.map.client-id}")
  private String naverClientId;

  @GetMapping("/region/update")
  public String checkRegionPage(Model model) {

    Region region = userService.authenticatedUser().getRegion();
    if (region != null) {
      model.addAttribute("currentAddress", region.getAddress());
      model.addAttribute("currentLat", region.getLat());
      model.addAttribute("currentLng", region.getLng());
      log.info("lat: {}, lng: {}", region.getLat(), region.getLng());
    } else {
      model.addAttribute("currentAddress", "서울특별시");
      model.addAttribute("currentLat", 37.56661);
      model.addAttribute("currentLng", 126.978388);
    }
    model.addAttribute("naverClientId", naverClientId);
    return "checkRegion";
  }

  @GetMapping("/update")
  public String updateUserInfoPage(Model model) {
    //TODO 비밀번호 입력해야 view 이동 조건
    User user = userService.authenticatedUser();
    log.info("회원정보 수정 페이지 이동- SNS 로그인 여부: {}",
        CustomSecurityUtil.isAuthenticated() && user.getPassword() == null);
    //SNS(구글 로그인) 여부 전달

    model.addAttribute("isSNSLogin",
        CustomSecurityUtil.isAuthenticated() && user.getPassword() == null);

    model.addAttribute("formData", SignUpRequest.builder()
        .username(user.getUsername())
        .password("")
        .passwordConfirm("")
        .nickname(user.getNickname())
        .build());
    return "setUserProfile";
  }

  @PostMapping("/runner-level/update")
  @ResponseBody
  public void changeRunnerLevel(@RequestBody RunnerLevelRequest request) {
    log.warn("러너 레벨 변경 진행- 러너 레벨: {}", request.getRunnerLevel().name());
    userService.updateRunnerLevel(request);
  }

  @PostMapping("/region/update")
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
    if (request.getUsername() != null && !request.getUsername().equals(user.getLoginId())
        && userService.isLoginIdExist(
        request.getUsername())) {
      model.addAttribute("formData", request);
      model.addAttribute("loginIdErrorMessage", "이미 존재하는 아이디입니다.");
      return "setUserProfile";
    }

    if (!request.getNickname().equals(user.getNickname())
        && userService.isNicknameExist(request.getNickname())) {
      model.addAttribute("formData", request);
      model.addAttribute("nickNameErrorMessage", "이미 존재하는 닉네임입니다.");
      return "setUserProfile";
    }
    if (request.getPassword() != null && !request.getPassword()
        .equals(request.getPasswordConfirm())) {
      model.addAttribute("formData", request);
      return "setUserProfile";
    }
    userService.updateUserInfo(request);
    return "redirect:/main";
  }


}
