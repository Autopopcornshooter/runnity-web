package runnity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

@Entity
@Getter
public class ScheduleParticipant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "schedule_participant_id", unique = true, updatable = false)
  private Long scheduleParticipantId;

  @Column(name = "participant_status")
  private ParticipantStatus participantStatus;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_room_member_id")
  private ChatRoomMember chatRoomMember;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "schedule_id")
  private Schedule schedule;

}
