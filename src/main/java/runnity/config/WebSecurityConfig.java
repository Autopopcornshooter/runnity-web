package runnity.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import runnity.UserRole;
import runnity.handler.LoginSuccessHandler;
import runnity.service.CustomOAuth2UserService;


@EnableWebSecurity
@Configuration
@AllArgsConstructor
@Slf4j
public class WebSecurityConfig {

  LoginSuccessHandler loginSuccessHandler;
  CustomOAuth2UserService customOAuth2UserService;

  @Bean
  public WebSecurityCustomizer configure() {
    return web -> web.ignoring()
        .requestMatchers("/h2-console/**", "/images/**",
            "/css/**", "/js/**");
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain userSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity.
        securityMatcher("/**")
        .authorizeHttpRequests(auth ->
            auth
                .requestMatchers(
                    "/"
                    , "/login/oauth2/**"
                    , "/api/auth/signIn"
                    , "/api/auth/signUp"
                    , "/api/auth/access-denied"
                    , "/api/auth/signOut"
                    , "/h2-console/**"
                    , "/image/**"
                    , "/css/**"
                    , "/js/**"
                    , "/ws-chat/**"
                    , "/ws-stomp/**")
                .permitAll()
                .requestMatchers("/.well-known/**").permitAll()
                .anyRequest().hasAuthority(UserRole.ROLE_USER.name()))
        .formLogin(auth ->
            auth
                .loginPage("/api/auth/signIn")
                .successHandler(loginSuccessHandler))
        .oauth2Login(oauth2 ->
            oauth2
                .loginPage("/api/auth/signIn")
                .successHandler(loginSuccessHandler)
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)))// 구글 로그인 DB 연동 처리
        .logout(auth ->
            auth
                .logoutUrl("/api/auth/signOut")
                .logoutSuccessUrl("/api/auth/signIn?signOut")
                .invalidateHttpSession(true)
                .clearAuthentication(true))
        .exceptionHandling(exception ->
            exception
                .accessDeniedPage("/api/auth/access-denied"));

    httpSecurity.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

    log.info("SecurityFilterChain Applied");
    return httpSecurity.build();

  }


}