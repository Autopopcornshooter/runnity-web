package runnity.dto;

import lombok.Getter;
import lombok.Setter;
import runnity.domain.ParticipantStatus;

@Getter
@Setter
public class ParticipantRequest {

  private ParticipantStatus participantStatus;
  private Long memberId;
  private Long scheduleId;
}

