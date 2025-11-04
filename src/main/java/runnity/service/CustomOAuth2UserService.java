package runnity.service;

import java.util.Collections;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import runnity.RunnerLevel;
import runnity.UserRole;
import runnity.domain.User;
import runnity.dto.CustomOAuth2User;
import runnity.exceptions.UserNotFoundException;
import runnity.repository.UserRepository;

@Service
@AllArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(request);

    String email = oAuth2User.getAttribute("email");
    String name = oAuth2User.getAttribute("name");

    User user = userRepository.findByLoginId(email)
        .orElseGet(() -> userRepository.save(
            User.builder()
                .loginId(email)
                .nickname(name)
                .userRole(UserRole.ROLE_USER)
                .runnerLevel(RunnerLevel.BEGINNER)
                .build()
        ));
    Map<String, Object> attributes = oAuth2User.getAttributes();
    log.info("구글 로그인 시도- email: {}", email);
    return new CustomOAuth2User(
        Collections.singleton(UserRole.ROLE_USER::name),
        attributes,
        "email",
        user.getUserId()
    );

  }

}
