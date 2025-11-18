package runnity.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import runnity.domain.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

  Optional<Schedule> findTop1ByChatRoom_ChatRoomIdOrderByCreatedAtDesc(Long roomId);

  List<Schedule> findByChatRoom_ChatRoomIdOrderByStartAtDesc(Long roomId);
}
