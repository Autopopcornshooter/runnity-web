package runnity.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import runnity.domain.User;
import runnity.exceptions.UserNotFoundException;
import runnity.repository.UserRepository;
import runnity.util.CustomSecurityUtil;

@Component
@AllArgsConstructor
@Slf4j
public class FormLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final UserRepository userRepository;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    String loginId = CustomSecurityUtil.getCurrentUserLoginId();
    log.info("Login Success- loginId: " + loginId);
    User user = userRepository.findByLoginId(loginId)
        .orElseThrow(() -> new UserNotFoundException(loginId));

    getRedirectStrategy().sendRedirect(request, response, "/api/auth/main");

  }
}
