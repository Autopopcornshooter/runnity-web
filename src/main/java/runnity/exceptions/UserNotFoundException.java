package runnity.exceptions;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(Long id) {
    super("해당 아이디의 유저가 존재하지 않습니다. : " + id);
  }

  public UserNotFoundException(String loginId) {
    super("해당 아이디의 유저가 존재하지 않습니다. : " + loginId);
  }
}
