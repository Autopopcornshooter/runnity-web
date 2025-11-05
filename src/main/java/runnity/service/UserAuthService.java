package runnity.service;

import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import runnity.UserRole;
import runnity.domain.User;
import runnity.domain.UserMatchState;
import runnity.dto.SignUpRequest;
import runnity.exceptions.UserNotFoundException;
import runnity.repository.UserRepository;
import runnity.util.CustomSecurityUtil;

@Service
@AllArgsConstructor
@Slf4j
public class UserAuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder encoder;

  public User saveUserInfo(SignUpRequest request) {
    return userRepository.save(
        User.builder()
            .nickname(request.getNickname())
            .loginId(request.getUsername())
            .password(encoder.encode(request.getPassword()))
            .createdAt(request.getCreatedAt())
            .updatedAt(request.getUpdatedAt())
            .runnerLevel(request.getRunnerLevel())
            .userRole(UserRole.ROLE_USER)
            .matchState(UserMatchState.IDLE)
            .build()
    );
  }

  public boolean isLoginIdExist(String loginId) {
    return userRepository.existsByLoginId(loginId);
  }

  public boolean isNicknameExist(String nickName) {
    return userRepository.existsByNickname(nickName);
  }

  public void deleteUserInfo(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
    userRepository.delete(user);
  }

  public User authenticatedUser() {
    return userRepository.findByLoginId(CustomSecurityUtil.getCurrentUserLoginId())
        .orElseThrow(() -> new UserNotFoundException(CustomSecurityUtil.getCurrentUserLoginId()));
  }


}
