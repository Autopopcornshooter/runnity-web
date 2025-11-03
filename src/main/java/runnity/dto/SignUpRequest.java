package runnity.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import runnity.RunnerLevel;

@Getter
@Setter
public class SignUpRequest {

  private String nickname;
  private String username;
  private String password;
  private String passwordConfirm;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private RunnerLevel runnerLevel;
}
