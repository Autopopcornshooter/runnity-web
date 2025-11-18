package runnity.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import runnity.domain.ParticipantStatus;
import runnity.domain.Region;
import runnity.domain.Schedule;

@Getter
@Setter
@Builder
public class ScheduleResponse {

  private Long scheduleId;
  private String title;
  private String detail;
  private LocalDateTime startAt;
  private String location;
  private Long scheduleCreatorId;
  private Long memberId;
  private boolean isCreator;

  private int yesCount;
  private int noCount;

  private ParticipantStatus myStatus;

  public static ScheduleResponse from(
      Schedule schedule, int yes, int no,
      ParticipantStatus myStatus, boolean isCreator, Long memberId) {

    return ScheduleResponse.builder()
        .scheduleId(schedule.getScheduleId())
        .title(schedule.getTitle())
        .detail(schedule.getDetail())
//        .location(schedule.getRegion().getAddress())
        .startAt(schedule.getStartAt())
        .scheduleCreatorId(schedule.getScheduleCreator().getChatRoomMemberId())
        .memberId(memberId)
        .isCreator(isCreator)
        .yesCount(yes)
        .noCount(no)
        .myStatus(myStatus)
        .build();
  }

}
