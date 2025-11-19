package runnity.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import runnity.domain.User;
import runnity.repository.UserRepository;


public class CustomSecurityUtil {


  //로그인 되어있는 유저 로그인 id 반환
  public static String getCurrentUserLoginId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof UserDetails userDetails) {
      return userDetails.getUsername();   //일반로그인 시 loginId 반환
    }

    if (principal instanceof OAuth2User oAuth2User) {
      return oAuth2User.getAttributes().get("email").toString(); //Google 로그인 시 loginId 반환
    }
    return null;
  }


  //로그인 되어있는지 판별
  public static boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }
}
