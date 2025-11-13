package runnity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import runnity.domain.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

}
