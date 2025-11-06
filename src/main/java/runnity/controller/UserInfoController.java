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
import runnity.dto.ChangeRegionRequest;
import runnity.dto.SignUpRequest;
import runnity.service.RegionService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/userInfo")
@Slf4j
public class UserInfoController {

  private final RegionService regionService;

  @Value("${naver.map.client-id}")
  private String naverClientId;

  @GetMapping("/region")
  public String checkRegionPage(Model model) {
    model.addAttribute("naverClientId", naverClientId);
    return "checkRegion";
  }

  @PostMapping("/region")
  public String changeRegion(ChangeRegionRequest request) {
    regionService.changeRegion(request);
    log.info("지역 정보 변경 : {}",
        request.getAddress() + " " + request.getLat().toString() + " " + request.getLng()
            .toString());
    return "redirect:/main";
  }

}
