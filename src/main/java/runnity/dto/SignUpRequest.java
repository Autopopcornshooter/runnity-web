package runnity.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import runnity.RunnerLevel;

@Getter
@Setter
public class SignUpRequest {

  private String nickname;
  private String loginId;
  private String password;
  private LocalDateTime createdAt;
  private RunnerLevel runnerLevel;
}
