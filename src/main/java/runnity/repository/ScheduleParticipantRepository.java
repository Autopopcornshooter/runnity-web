package runnity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import runnity.domain.ScheduleParticipant;

public interface ScheduleParticipantRepository extends JpaRepository<ScheduleParticipant, Long> {

}
