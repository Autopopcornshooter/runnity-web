package runnity.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Service;
import runnity.domain.User;
import runnity.dto.SignUpRequest;
import runnity.exceptions.UserNotFoundException;
import runnity.repository.UserRepository;

@Service
@AllArgsConstructor
public class UserAuthService {

  private final UserRepository userRepository;

  public void saveUserInfo(SignUpRequest request) {
    userRepository.save(
        User.builder()
            .nickname(request.getNickname())
            .loginId(request.getLoginId())
            .password(request.getPassword())
            .createdAt(request.getCreatedAt())
            .runnerLevel(request.getRunnerLevel())
            .build()
    );
  }

  public void deleteUserInfo(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
    userRepository.delete(user);
  }


}
