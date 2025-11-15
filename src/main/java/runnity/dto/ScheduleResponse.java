package runnity.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import runnity.domain.Region;

@Getter
@Setter
@Builder
public class ScheduleResponse {

  private String title;
  private String detail;
  private LocalDateTime startAt;
  private Long scheduleCreatorId;
  private Region region;
}
