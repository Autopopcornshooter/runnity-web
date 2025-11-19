package runnity.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import runnity.domain.ParticipantStatus;
import runnity.domain.ScheduleParticipant;

public interface ScheduleParticipantRepository extends JpaRepository<ScheduleParticipant, Long> {


  Optional<ScheduleParticipant> findBySchedule_ScheduleIdAndChatRoomMember_ChatRoomMemberId(
      Long scheduleId, Long chatRoomMemberId);

  int countBySchedule_ScheduleIdAndParticipantStatus(Long scheduleId, ParticipantStatus status);
}
