package runnity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import runnity.service.ScheduleParticipantService;

@Controller
@RequiredArgsConstructor
public class ScheduleParticipantController {

  ScheduleParticipantService participantService;

}
