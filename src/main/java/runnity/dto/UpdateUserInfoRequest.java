package runnity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserInfoRequest {

  private String username;
  private String password;
  private String passwordConfirm;
  private String nickname;

}
