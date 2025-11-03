package runnity.service;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import runnity.UserRole;
import runnity.domain.User;
import runnity.dto.SignUpRequest;
import runnity.exceptions.UserNotFoundException;
import runnity.repository.UserRepository;

@Service
@AllArgsConstructor
public class UserAuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder encoder;

  public void saveUserInfo(SignUpRequest request) {
    userRepository.save(
        User.builder()
            .nickname(request.getNickname())
            .loginId(request.getUsername())
            .password(encoder.encode(request.getPassword()))
            .createdAt(request.getCreatedAt())
            .updatedAt(request.getUpdatedAt())
            .runnerLevel(request.getRunnerLevel())
            .userRole(UserRole.ROLE_USER)
            .build()
    );
  }

  public boolean isLoginIdExist(String loginId){
    return userRepository.existsByLoginId(loginId);
  }
  public void deleteUserInfo(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
    userRepository.delete(user);
  }


}
