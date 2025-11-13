package runnity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateScheduleRequest {

  private Long roomId;
  private String title;
  private String detail;

}
