package runnity.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import runnity.RunnerLevel;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {

  private String nickname;
  private String profileImageUrl;
  private String username;
  private String password;
  private String passwordConfirm;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private RunnerLevel runnerLevel;
  private String address;
  private Double lat;
  private Double lng;
}
