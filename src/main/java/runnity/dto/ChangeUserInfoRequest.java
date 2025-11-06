package runnity.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import runnity.RunnerLevel;

@Getter
@Setter
public class ChangeUserInfoRequest {

  private String nickname;
  private String username;
  private String password;
  private String passwordConfirm;
  private LocalDateTime updatedAt;
  private RunnerLevel runnerLevel;
  private String address;
  private Double lat;
  private Double lng;
}
