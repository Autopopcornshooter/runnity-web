package runnity.service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import runnity.domain.ChatRoom;
import runnity.domain.ChatRoomMember;
import runnity.domain.Schedule;
import runnity.domain.User;
import runnity.dto.CreateScheduleRequest;
import runnity.exceptions.UserNotFoundException;
import runnity.repository.ChatRoomMemberRepository;
import runnity.repository.ChatRoomRepository;
import runnity.repository.ScheduleRepository;
import runnity.repository.UserRepository;
import runnity.util.CustomSecurityUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

  private final ScheduleRepository scheduleRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final UserRepository userRepository;
  private final ChatRoomMemberRepository chatRoomMemberRepository;

  @Transactional
  public Schedule createSchedule(Long roomId, CreateScheduleRequest request) {
    if (!roomId.equals(request.getRoomId())) {
      log.error("Room Id Not match");
      return null;
    }
    Schedule schedule = Schedule.builder()
        .title(request.getTitle())
        .detail(request.getDetail())
        .chatRoom(getChatRoom(request))
        .createdAt(LocalDateTime.now())
        .scheduleCreator(getCreator(request))
        .build();

    return scheduleRepository.save(schedule);
  }

  public ChatRoom getChatRoom(CreateScheduleRequest request) {
    return chatRoomRepository.findById(request.getRoomId()).orElseThrow(
        () -> new IllegalArgumentException("ChatRoom not found - roomId: " + request.getRoomId()));
  }

  public ChatRoomMember getCreator(CreateScheduleRequest request) {
    return chatRoomMemberRepository.findByRoomAndUser(request.getRoomId(),
        getAuthenticatedUser().getUserId()).orElseThrow(
        () -> new IllegalArgumentException("Member Not found- Login Id: " +
            getAuthenticatedUser().getUserId() + " Room Id: " + request.getRoomId()));
  }

  public User getAuthenticatedUser() {
    return userRepository.findByLoginId(CustomSecurityUtil.getCurrentUserLoginId())
        .orElseThrow(() -> new UserNotFoundException(CustomSecurityUtil.getCurrentUserLoginId()));
  }

}
