package runnity.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import runnity.UserRole;
import runnity.domain.Region;
import runnity.domain.User;
import runnity.domain.UserMatchState;
import runnity.dto.ChangeUserInfoRequest;
import runnity.dto.SignUpRequest;
import runnity.exceptions.UserNotFoundException;
import runnity.repository.RegionRepository;
import runnity.repository.UserRepository;
import runnity.util.CustomSecurityUtil;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;
  private final RegionRepository regionRepository;
  private final PasswordEncoder encoder;

  public User saveUserInfo(SignUpRequest request) {

    Region region = regionRepository.findByLatAndLng(request.getLat(), request.getLng())
        .orElseGet(() -> {
          Region newRegion = Region.builder()
              .address(request.getAddress())
              .lat(request.getLat())
              .lng(request.getLng())
              .build();
          return regionRepository.save(newRegion);
        });

    return userRepository.save(
        User.builder()
            .nickname(request.getNickname())
            .loginId(request.getUsername())
            .password(encoder.encode(request.getPassword()))
            .createdAt(request.getCreatedAt())
            .updatedAt(request.getUpdatedAt())
            .runnerLevel(request.getRunnerLevel())
            .region(region)
            .userRole(UserRole.ROLE_USER)
            .matchState(UserMatchState.IDLE)
            .build()
    );
  }

  public void changeUserInfo(ChangeUserInfoRequest request) {
    User user = authenticatedUser();

    if (!request.getAddress().isBlank() && !request.getLat().isNaN() && !request.getLng().isNaN()) {
      Region region = regionRepository.findByLatAndLng(request.getLat(), request.getLng())
          .orElseGet(() -> {
            Region newRegion = Region.builder()
                .address(request.getAddress())
                .lat(request.getLat())
                .lng(request.getLng())
                .build();
            return regionRepository.save(newRegion);
          });
      user.setRegion(region);
    }
    if (!request.getNickname().isBlank()) {

    }
    userRepository.save(user);
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
