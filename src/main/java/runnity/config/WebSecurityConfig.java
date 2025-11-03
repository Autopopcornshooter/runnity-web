package runnity.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import runnity.UserRole;


@EnableWebSecurity
@Configuration
@AllArgsConstructor
@Slf4j
public class WebSecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;


  @Bean
  public WebSecurityCustomizer configure() {
    return web -> web.ignoring()
        .requestMatchers("/static/**", "/templates/**", "/h2-console/**", "/images/**", "/error",
            "/css/**", "/js/**");
  }

  @Bean
  public AuthenticationManager authenticationmanager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

//  @Bean
//  @Order(1) //for Test
//  public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
//    log.info("SecurityFilterChain1 enabled");
//    http
//        .securityMatcher("/**") // 웹 페이지 전체에 적용(테스트용)
//        .csrf(AbstractHttpConfigurer::disable)
//        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//        .authorizeHttpRequests(auth -> auth
//            .requestMatchers(
//                "/**"
//            ).permitAll()
//            .anyRequest().permitAll() // HTML은 모두 허용
//        );
//    return http.build();
//  }

  @Bean
  @Order(2)
  public SecurityFilterChain userSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
    log.info("SecurityFilterChain2 enabled");
    httpSecurity
        .securityMatcher("/api/**")
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // ★ 세션 금지
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/signIn", "/api/auth/signUp", "/api/auth/refresh")
            .permitAll()
            .anyRequest().hasAuthority("ROLE_USER"))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return httpSecurity.build();

  }


}