package runnity.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import runnity.dto.CreateScheduleRequest;
import runnity.repository.ScheduleRepository;
import runnity.service.ScheduleService;

@Controller
@RequiredArgsConstructor
public class ScheduleController {

  private final ScheduleService scheduleService;


  @PostMapping("/chat-rooms/{roomId}/create-schedule")
  public ResponseEntity<?> createSchedule(@PathVariable Long roomId,
      @RequestBody CreateScheduleRequest request) {
    scheduleService.createSchedule(roomId, request);
    return ResponseEntity.ok().build();
  }
}
