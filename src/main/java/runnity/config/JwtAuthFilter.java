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
    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (auth != null && auth.startsWith("Bearer ")) {
      String token = auth.substring(7);
      try {
        if (jwt.isAccessToken(token)) {
          Claims claims = jwt.parse(token).getBody();
          String username = claims.getSubject();
          //User_Roles 가져오기
          List<String> roles = claims.get("roles", List.class);
          //추출한 User_Roles로 권한 생성
          List<SimpleGrantedAuthority> userAuthorities = roles.stream()
              .map(SimpleGrantedAuthority::new)
              .toList();
          log.info("Jwt roles" + roles);

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(username, null, userAuthorities);
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } catch (Exception ignored) {

      }

    }
    chain.doFilter(request, response);
  }

}
