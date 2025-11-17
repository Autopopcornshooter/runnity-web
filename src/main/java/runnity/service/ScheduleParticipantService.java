package runnity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import runnity.repository.ScheduleParticipantRepository;

@Service
@RequiredArgsConstructor
public class ScheduleParticipantService {

  private final ScheduleParticipantRepository participantRepository;
}
