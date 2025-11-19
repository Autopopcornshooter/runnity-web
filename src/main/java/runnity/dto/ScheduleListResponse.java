package runnity.dto;

import lombok.Builder;
import lombok.Getter;
import runnity.domain.Schedule;

@Getter
@Builder
public class ScheduleListResponse {

  private Long scheduleId;
  private String title;
  private String startAt;

  public static ScheduleListResponse from(Schedule schedule) {
    return ScheduleListResponse.builder()
        .scheduleId(schedule.getScheduleId())
        .title(schedule.getTitle())
        .startAt(schedule.getStartAt().toString()) // "2025-11-19T12:30"
        .build();
  }
}
