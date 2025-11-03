package runnity.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import runnity.config.JwtTokenService;
import runnity.dto.LoginRequest;
import runnity.dto.LoginResponse;
import runnity.dto.SignUpRequest;
import runnity.repository.InMemoryRefreshStore;
import runnity.service.UserAuthService;

@Controller
@AllArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class UserAuthController {

  private final UserAuthService userAuthService;
  private final AuthenticationManager authManager;
  private final InMemoryRefreshStore refreshStore;
  private final JwtTokenService jwtService;


  @GetMapping("/signIn")
  public String signInPage(Authentication authentication) {
    return "signIn";
  }

  @GetMapping("/signUp")
  public String signUpPage(Authentication authentication) {
    return "signUp";
  }

  @GetMapping("/test")
  public String testPage() {
    return "test";
  }

  @PostMapping("/signIn")
  public ResponseEntity<?> signIn(@RequestBody LoginRequest request,
      HttpServletResponse response) {

    String username = request.username();
    String password = request.password();

    var auth = authManager.authenticate(
        new UsernamePasswordAuthenticationToken(username, password));

    var roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

    String access = jwtService.generateAccessToken(auth.getName(), roles);
    String refresh = jwtService.generateRefreshToken(auth.getName(), UUID.randomUUID().toString());

    // ★ Access 토큰 쿠키
    response.addHeader("Set-Cookie",
        ResponseCookie.from("access_token", access)
            .httpOnly(true)
            .secure(false)        // 로컬 http면 false, 배포 https면 true
            .sameSite("Lax")      // 크로스 도메인이면 None+secure
            .path("/")            // 전역
            .maxAge(Duration.ofMinutes(30)) // access 만료에 맞춰
            .build().toString());

    // (선택) Refresh 토큰 쿠키
    response.addHeader("Set-Cookie",
        ResponseCookie.from("refresh_token", refresh)
            .httpOnly(true).secure(false).sameSite("Lax").path("/")
            .maxAge(Duration.ofDays(7))
            .build().toString());

    // ★ 페이지 이동
    return ResponseEntity.ok(Map.of("accessToken", access));

//    Authentication auth = authManager.authenticate(
//        new UsernamePasswordAuthenticationToken(username, password)
//    );
//
//    List<String> roles = auth.getAuthorities().stream()
//        .map(GrantedAuthority::getAuthority)
//        .toList();
//    log.info("User Roles: " + roles);
//    // JWT 발급
//    String access = jwtService.generateAccessToken(auth.getName(),
//        auth.getAuthorities().stream()
//            .map(GrantedAuthority::getAuthority).
//            toList());
//    String refreshJti = UUID.randomUUID().toString();
//    String refresh = jwtService.generateRefreshToken(auth.getName(), refreshJti);
//    log.info("JWT 발급(Access/Refresh)");
//
//    ResponseCookie cookie = ResponseCookie.from("refresh_token", refresh)
//        .httpOnly(true)
//        .secure(false)
//        .path("/")
//        .sameSite("Lax")
//        .build();
//    response.addHeader("Set-Cookie", cookie.toString());
//
//    return "redirect:/api/auth/test";

//
//    // Refresh 보관 (만료는 토큰 exp와 동일 주기 관리 권장)
//    Instant exp = jwtService.parse(refresh).getBody().getExpiration().toInstant();
//    refreshStore.save(refreshJti, username, exp);
//    log.info("Refresh 저장소 보관");
//    log.info(refresh);
//
//    // HttpOnly Secure 쿠키로 전달(프론트가 JS로 못 읽게)
//    ResponseCookie cookie = ResponseCookie.from("refresh_token", refresh)
//        .httpOnly(true)
//        .secure(true)
//        .path("/")
//        .sameSite("Lax") // 프론트 상황에 맞게 Lax/None
//        .build();
//    response.addHeader("Set-Cookie", cookie.toString());
//    log.info("Refresh token 쿠키로 전달");
//
//    long expiresIn =
//        jwtService.parse(access).getBody().getExpiration().getTime() / 1000 - Instant.now()
//            .getEpochSecond();
//    log.info("만료 시점 지정");
//    return ResponseEntity.ok(new LoginResponse(access, expiresIn));
  }

  @PostMapping("/refresh")
  public ResponseEntity<LoginResponse> refresh(
      @CookieValue(name = "refresh_token", required = false) String refreshCookie,
      HttpServletResponse response) {
    if (refreshCookie == null) {
      return ResponseEntity.status(401).build();
    }
    var jws = jwtService.parse(refreshCookie);
    if (!jwtService.isRefreshToken(refreshCookie)) {
      return ResponseEntity.status(401).build();
    }

    Claims claims = jws.getBody();
    String oldJti = claims.getId();
    var entry = refreshStore.get(oldJti);
    if (entry == null || entry.revoked() || entry.expiresAt().isBefore(Instant.now())) {
      return ResponseEntity.status(401).build();
    }

    String username = claims.getSubject();
    // 필요한 경우 DB에서 권한 재조회
    List<String> roles = List.of("ROLE_USER");

    // Access 재발급
    String newAccess = jwtService.generateAccessToken(username, roles);
    log.info("Access Token 재발급");

    // Refresh 회전(rotate) 권장
    String newJti = UUID.randomUUID().toString();
    String newRefresh = jwtService.generateRefreshToken(username, newJti);
    Instant newExp = jwtService.parse(newRefresh).getBody().getExpiration().toInstant();
    refreshStore.rotate(oldJti, newJti, username, newExp);
    log.info("Refresh rotate 수행");

    ResponseCookie cookie = ResponseCookie.from("refresh_token", newRefresh)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .sameSite("Strict")
        .maxAge(newExp.getEpochSecond() - Instant.now().getEpochSecond())
        .build();
    response.addHeader("Set-Cookie", cookie.toString());
    log.info("쿠키 전달");

    long expiresIn =
        jwtService.parse(newAccess).getBody().getExpiration().getTime() / 1000 - Instant.now()
            .getEpochSecond();
    log.info("만료 시점 지정");
    return ResponseEntity.ok(new LoginResponse(newAccess, expiresIn));
  }

  @PostMapping("/signOut")
  public ResponseEntity<Void> logout(
      @CookieValue(name = "refresh_token", required = false) String refreshCookie,
      HttpServletResponse res) {
    if (refreshCookie != null) {
      try {
        var jws = jwtService.parse(refreshCookie);
        refreshStore.revoke(jws.getBody().getId());
        log.info("로그아웃 성공");
      } catch (Exception ignored) {
        log.info("로그아웃 실패");
      }
    }
    // 쿠키 제거
    ResponseCookie expired = ResponseCookie.from("refresh_token", "")
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(0).build();
    res.addHeader("Set-Cookie", expired.toString());
    log.info("쿠키 제거");
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/signUp")
  public String signUp(SignUpRequest request, Model model) {

    log.info(request.getNickname());

    if (userAuthService.isLoginIdExist(request.getUsername())) {
      model.addAttribute("formData", request);
      model.addAttribute("errorMessage", "이미 존재하는 아이디입니다.");
      return "signUp";
    }
    if (!request.getPassword().equals(request.getPasswordConfirm())) {
      model.addAttribute("formData", request);
      return "signUp";
    }
    userAuthService.saveUserInfo(request);
    return "redirect:/api/auth/signIn";
  }


}
