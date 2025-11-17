package runnity.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import runnity.domain.Region;
import runnity.domain.User;
import runnity.dto.ChangeRegionRequest;
import runnity.exceptions.UserNotFoundException;
import runnity.repository.RegionRepository;
import runnity.repository.UserRepository;
import runnity.util.CustomSecurityUtil;

@Service
@AllArgsConstructor
public class RegionService {

  RegionRepository regionRepository;
  UserRepository userRepository;

  public void changeRegion(ChangeRegionRequest request) {
    Region region = regionRepository.findByLatAndLng(request.getLat(), request.getLng())
        .orElseGet(() -> {
          Region newRegion = Region.builder()
              .address(request.getAddress())
              .lat(request.getLat())
              .lng(request.getLng())
              .build();
          return regionRepository.save(newRegion);
        });
    User user = userRepository.findByLoginId(CustomSecurityUtil.getCurrentUserLoginId())
        .orElseThrow(() -> new UserNotFoundException(CustomSecurityUtil.getCurrentUserLoginId()));
    user.setRegion(region);
    userRepository.save(user);
  }
}
