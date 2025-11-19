package runnity.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateScheduleRequest {

  private Long roomId;

  private String title;
  private LocalDateTime startAt;
  private String detail;

  private Long regionId;
  private Double lat;
  private Double lng;
  private String address;

}
