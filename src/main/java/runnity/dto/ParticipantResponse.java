package runnity.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import runnity.domain.ParticipantStatus;
import runnity.domain.ScheduleParticipant;

@Getter
@Setter
@Builder
public class ParticipantResponse {

  private ParticipantStatus participantStatus;
  private Long memberId;
  private Long scheduleId;
  private int yesCount;
  private int noCount;

  public static ParticipantResponse from(ScheduleParticipant participant) {
    return ParticipantResponse.builder()
        .participantStatus(participant.getParticipantStatus())
        .memberId(participant.getChatRoomMember().getChatRoomMemberId())
        .scheduleId(participant.getSchedule().getScheduleId())
        .build();
  }
}
