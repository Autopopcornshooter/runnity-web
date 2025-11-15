package runnity.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import runnity.dto.CreateScheduleRequest;
import runnity.dto.ScheduleResponse;
import runnity.repository.ScheduleRepository;
import runnity.service.ScheduleService;

@Controller
@RequiredArgsConstructor
public class ScheduleController {

  private final ScheduleService scheduleService;

  //일정 체크 창 열기 + 데이터 전달
  @GetMapping("/chat-rooms/{roomId}/schedules/recent")
  public ResponseEntity<ScheduleResponse> openRecentSchedule(@PathVariable Long roomId) {
    ScheduleResponse response = scheduleService.getRecentSchedule(roomId);

    if (response == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(response);
  }

  //일정 생성 => 리포지토리 저장
  @PostMapping("/chat-rooms/{roomId}/create-schedule")
  public ResponseEntity<?> createSchedule(@PathVariable Long roomId,
      @RequestBody CreateScheduleRequest request) {
    scheduleService.createSchedule(roomId, request);
    return ResponseEntity.ok().build();
  }
}
