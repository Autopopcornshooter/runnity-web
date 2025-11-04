package runnity.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import runnity.domain.User;
import runnity.exceptions.UserNotFoundException;
import runnity.repository.UserRepository;

@Service
@AllArgsConstructor
public class
CustomUserDetailService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UserNotFoundException {
    return userRepository.findByLoginId(username)
        .orElseThrow(() -> new UserNotFoundException(username));
  }


}
