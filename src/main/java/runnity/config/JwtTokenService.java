package runnity.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtTokenService {

  //JWT 토근 발급 및 검증
  private final Key key;
  private final String issuer;
  private final long accessTtl;
  private final long refreshTtl;

  public JwtTokenService(
      @Value("${security.jwt.secret}") String secret,
      @Value("${security.jwt.issuer}") String issuer,
      @Value("${security.jwt.access-token-ttl-seconds}") long accessTtl,
      @Value("${security.jwt.refresh-token-ttl-seconds}") long refreshTtl) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
    this.issuer = issuer;
    this.accessTtl = accessTtl;
    this.refreshTtl = refreshTtl;
  }

  public String generateAccessToken(String subject, List<String> roles) {
    Instant now = Instant.now();
    log.info("Access Token Generated at :" + now);
    return Jwts.builder()
        .setId(UUID.randomUUID().toString())
        .setSubject(subject)
        .setIssuer(issuer)
        .setAudience("access")
        .claim("roles", roles)
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(accessTtl)))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateRefreshToken(String subject, String refreshJti) {
    Instant now = Instant.now();
    log.info("Access Token Refreshed at :" + now);
    return Jwts.builder()
        .setId(refreshJti)
        .setSubject(subject)
        .setIssuer(issuer)
        .setAudience("refresh")
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(refreshTtl)))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public Jws<Claims> parse(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .requireIssuer(issuer)
        .build()
        .parseClaimsJws(token);
  }

  public boolean isAccessToken(String token) {
    try {
      return "access".equals(parse(token).getBody().getAudience());
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isRefreshToken(String token) {
    try {
      return "refresh".equals(parse(token).getBody().getAudience());
    } catch (Exception e) {
      return false;
    }
  }


}
