package runnity.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import runnity.dto.CreateScheduleRequest;
import runnity.dto.ScheduleListResponse;
import runnity.dto.ScheduleResponse;
import runnity.repository.ChatRoomMemberRepository;
import runnity.repository.ScheduleRepository;
import runnity.service.ScheduleService;
import runnity.util.CustomSecurityUtil;

@Controller
@RequiredArgsConstructor
public class ScheduleController {

  private final ScheduleService scheduleService;
  private final ChatRoomMemberRepository memberRepository;

  //일정 체크 창 열기 + 데이터 전달
  @GetMapping("/chat-rooms/{roomId}/schedules/recent")
  public ResponseEntity<ScheduleResponse> openRecentSchedule(@PathVariable Long roomId) {
    return ResponseEntity.ok(scheduleService.getRecentSchedule(roomId));
  }

  //일정 생성 => 리포지토리 저장
  @PostMapping("/chat-rooms/{roomId}/create-schedule")
  public ResponseEntity<?> createSchedule(@PathVariable Long roomId,
      @RequestBody CreateScheduleRequest request) {
    scheduleService.createSchedule(roomId, request);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/chat-rooms/{roomId}/schedules")
  public ResponseEntity<List<ScheduleListResponse>> getScheduleList(
      @PathVariable Long roomId) {

    return ResponseEntity.ok(
        scheduleService.getSchedules(roomId)
    );
  }

  // -----------------------------
  // 일정 상세
  // -----------------------------
  @GetMapping("/chat-rooms/{roomId}/schedules/{scheduleId}")
  public ResponseEntity<ScheduleResponse> getScheduleDetail(
      @PathVariable Long roomId,
      @PathVariable Long scheduleId) {

    // 유저 + 방 기준으로 매핑된 ChatRoomMember 조회
    Long memberId = memberRepository
        .findByRoomAndUser(scheduleService.getAuthenticatedUser().getUserId(), roomId)
        .orElseThrow(() -> new RuntimeException("ChatRoomMember not found"))
        .getChatRoomMemberId();

    return ResponseEntity.ok(
        scheduleService.getScheduleDetail(scheduleId, memberId)
    );
  }
}
