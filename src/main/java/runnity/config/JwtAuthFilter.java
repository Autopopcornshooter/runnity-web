package runnity.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@AllArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

  //Jwt 인증 필터
  //인증 요청마다 Access 검증
  private final JwtTokenService jwt;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {

    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    String token = null;

    if (header != null && header.startsWith("Bearer ")) {
      token = header.substring(7);
    } else if (request.getCookies() != null) {
      for (var c : request.getCookies()) {
        if ("access_token".equals(c.getName())) {            // ★ 쿠키에서 JWT 추출
          token = c.getValue();
          break;
        }
      }
    }

    if (token != null) {
      try {
        if (jwt.isAccessToken(token)) {
          var claims = jwt.parse(token).getBody();
          String username = claims.getSubject();
          List<String> roles = claims.get("roles", List.class);
          var auths = roles.stream()
              .map(SimpleGrantedAuthority::new)
              .toList();

          var authentication =
              new UsernamePasswordAuthenticationToken(username, null, auths);
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } catch (Exception e) {
        // 실패하면 그냥 통과 (익명 처리)
      }
    }

    chain.doFilter(request, response);
//    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
//    log.info("Authorization Header = {}", auth);
//    if (auth != null && auth.startsWith("Bearer ")) {
//      String token = auth.substring(7);
//      log.info("Parsed Token = {}", token);
//      try {
//        if (jwt.isAccessToken(token)) {
//          Claims claims = jwt.parse(token).getBody();
//          log.info("Claims = {}", claims);
//          String username = claims.getSubject();
//          //User_Roles 가져오기
//          List<String> roles = claims.get("roles", List.class);
//          log.info("Roles from JWT = {}", roles);
//          //추출한 User_Roles로 권한 생성
//          List<SimpleGrantedAuthority> userAuthorities = roles.stream()
//              .map(SimpleGrantedAuthority::new)
//              .toList();
//          log.info("Authorities = {}", userAuthorities);
//
//          UsernamePasswordAuthenticationToken authentication =
//              new UsernamePasswordAuthenticationToken(username, null, userAuthorities);
//          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//          SecurityContextHolder.getContext().setAuthentication(authentication);
//          log.info("Authentication before setContext = {}", authentication);
//        }
//      } catch (Exception e) {
//        log.warn("JWT parsing/validation filed: {}", e.getMessage());
//      }
//
//    }
//    chain.doFilter(request, response);

  }

}
