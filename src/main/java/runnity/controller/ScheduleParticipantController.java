package runnity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import runnity.dto.ParticipantRequest;
import runnity.dto.ParticipantResponse;
import runnity.service.ScheduleParticipantService;

@RestController
@RequiredArgsConstructor
public class ScheduleParticipantController {

  private final ScheduleParticipantService participantService;

  @PutMapping("/chat-rooms/joinSelect")
  public ResponseEntity<ParticipantResponse> selectJoin(@RequestBody ParticipantRequest request) {
    ParticipantResponse response = participantService.save(request);
    return ResponseEntity.ok(response);
  }
}
