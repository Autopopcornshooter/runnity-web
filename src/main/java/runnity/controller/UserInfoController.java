package runnity.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import runnity.domain.Region;
import runnity.domain.User;
import runnity.dto.ChangeRegionRequest;
import runnity.dto.RunnerLevelRequest;
import runnity.dto.SignUpRequest;
import runnity.dto.UpdateUserInfoRequest;
import runnity.service.ProfileImageService;
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
  private final ProfileImageService profileImageService;

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
  public String updateUserInfoPage(Model model, HttpSession session) {

    //재인증 절차 진행
    Boolean reAuth = Boolean.TRUE.equals(session.getAttribute("reAuth"));
    session.removeAttribute("reAuth");
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //SNS 로그인 여부 체크
    log.info("재인증 상태: {}", reAuth);
    if (auth instanceof UsernamePasswordAuthenticationToken && !reAuth) {
      return "redirect:/api/auth/re-auth";
    }
    //재인증 절차 완료

    User user = userService.authenticatedUser();
    log.info("회원정보 수정 페이지 이동- SNS 로그인 여부: {}",
        CustomSecurityUtil.isAuthenticated() && user.getPassword() == null);
    //SNS(구글 로그인) 여부 전달
    model.addAttribute("isSNSLogin",
        CustomSecurityUtil.isAuthenticated() && user.getPassword() == null);
    //프로필 이미지 null체크
    String profileImageUrl =
        (user.getProfileImage() == null || user.getProfileImage().getUrl().isBlank())
            ? "/images/runnity-person.png"
            : user.getProfileImage().getUrl();
    log.info("프로필 이미지 URL: {}", profileImageUrl);
    //현재 유저 정보 inputField 에 출력 (비밀번호 제외)
    model.addAttribute("formData", SignUpRequest.builder()
        .username(user.getUsername())
        .password("")
        .passwordConfirm("")
        .nickname(user.getNickname())
        .profileImageUrl(profileImageUrl)
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
  public String updateUserInfo(UpdateUserInfoRequest request, Model model, HttpSession session,
      @RequestParam("profile-image") MultipartFile image
      , @RequestParam("removeFlag") boolean removeFlag) throws IOException {
    User user = userService.authenticatedUser();
    //inputField 공란 체크 + 로그인 아이디 수정
    if (request.getUsername() != null && !request.getUsername().equals(user.getLoginId())
        && userService.isLoginIdExist(
        request.getUsername())) {
      model.addAttribute("formData", request);
      model.addAttribute("loginIdErrorMessage", "이미 존재하는 아이디입니다.");
      return "setUserProfile";
    }
    //inputField 공란 체크 + 별명 수정
    if (!request.getNickname().equals(user.getNickname())
        && userService.isNicknameExist(request.getNickname())) {
      model.addAttribute("formData", request);
      model.addAttribute("nickNameErrorMessage", "이미 존재하는 닉네임입니다.");
      return "setUserProfile";
    }
    //inputField 공란 체크 + 패스워드 수정
    if (request.getPassword() != null && !request.getPassword()
        .equals(request.getPasswordConfirm())) {
      model.addAttribute("formData", request);
      return "setUserProfile";
    }

    //프로필 이미지 업데이트/삭제
    if (removeFlag) {
      profileImageService.removeProfileImage(user);
    } else {
      if (!image.isEmpty()) {
        profileImageService.updateProfileImage(user, image);
      }
    }

    userService.updateUserInfo(request);
    log.info("저장 => 재인증 여부: {}", session.getAttribute("reAuth"));
    return "redirect:/main";
  }
}
