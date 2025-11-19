package runnity.service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import runnity.domain.ChatRoom;
import runnity.domain.ChatRoomMember;
import runnity.domain.ParticipantStatus;
import runnity.domain.Region;
import runnity.domain.Schedule;
import runnity.domain.ScheduleParticipant;
import runnity.domain.User;
import runnity.dto.CreateScheduleRequest;
import runnity.dto.ScheduleListResponse;
import runnity.dto.ScheduleResponse;
import runnity.exceptions.UserNotFoundException;
import runnity.repository.ChatRoomMemberRepository;
import runnity.repository.ChatRoomRepository;
import runnity.repository.RegionRepository;
import runnity.repository.ScheduleParticipantRepository;
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
  private final ScheduleParticipantRepository participantRepository;
  private final RegionRepository regionRepository;

  @Transactional
  public ScheduleResponse createSchedule(Long roomId, CreateScheduleRequest request) {
    if (!roomId.equals(request.getRoomId())) {
      log.error("Room Id Not match");
      return null;
    }
    Region region = null;
    if (request.getRegionId() != null) {
      region = regionRepository.findById(request.getRegionId()).orElse(null);
    } else if (request.getLat() != null && request.getLng() != null) {
      // 새로 Region 생성
      region = Region.builder()
          .address(request.getAddress())
          .lat(request.getLat())
          .lng(request.getLng())
          .build();
      regionRepository.save(region);
    }

    Long memberId = chatRoomMemberRepository.findByRoomAndUser(roomId,
        getAuthenticatedUser().getUserId()).get().getChatRoomMemberId();

    Schedule schedule = Schedule.builder()
        .title(request.getTitle())
        .detail(request.getDetail())
        .chatRoom(getChatRoom(request))
        .startAt(request.getStartAt())
        .region(region)
        .createdAt(LocalDateTime.now())
        .scheduleCreator(getCreator(request))
        .build();
    scheduleRepository.save(schedule);

    return ScheduleResponse.from(schedule, 0, 0, ParticipantStatus.PENDING, true,
        schedule.getScheduleCreator().getChatRoomMemberId());
  }


  public ScheduleResponse getRecentSchedule(Long roomId) {
//    Schedule recentSchedule = scheduleRepository.findTop1ByChatRoom_ChatRoomIdOrderByCreatedAtDesc(
//            roomId)
//        .orElseThrow(() -> new IllegalArgumentException("Room Id not found- ID: " + roomId));
//
//    return getSchedule(roomId, recentSchedule);
    Schedule schedule = scheduleRepository
        .findTop1ByChatRoom_ChatRoomIdOrderByCreatedAtDesc(roomId)
        .orElse(null);
    if (schedule == null) {
      return null;
    }
    Long memberId = chatRoomMemberRepository.findByRoomAndUser(roomId,
        getAuthenticatedUser().getUserId()).get().getChatRoomMemberId();

    ParticipantStatus myStatus = participantRepository
        .findBySchedule_ScheduleIdAndChatRoomMember_ChatRoomMemberId(schedule.getScheduleId(),
            memberId)
        .map(ScheduleParticipant::getParticipantStatus)
        .orElse(ParticipantStatus.PENDING);

    int yesCount = participantRepository.countBySchedule_ScheduleIdAndParticipantStatus(
        schedule.getScheduleId(), ParticipantStatus.JOINED);

    int noCount = participantRepository.countBySchedule_ScheduleIdAndParticipantStatus(
        schedule.getScheduleId(), ParticipantStatus.DECLINED);

    boolean isCreator = schedule.getScheduleCreator().getChatRoomMemberId().equals(memberId);

    return ScheduleResponse.from(schedule, yesCount, noCount, myStatus, isCreator, memberId);
  }

//  public ScheduleResponse getSchedule(Long roomId, Schedule schedule) {
//    boolean isCreator = schedule.getScheduleCreator().getChatRoomMemberId()
//        .equals(getMyMemberId(roomId));
//
//    return ScheduleResponse.builder()
//        .title(schedule.getTitle())
//        .detail(schedule.getDetail())
//        .region(schedule.getRegion())
//        .startAt(schedule.getStartAt())
//        .isCreator(isCreator)
//        .scheduleCreatorId(schedule.getScheduleCreator().getChatRoomMemberId())
//        .build();
//  }

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

  public Long getMyMemberId(Long roomId) {
    return chatRoomMemberRepository.findByRoomAndUser(roomId, getAuthenticatedUser().getUserId())
        .orElseThrow(() -> new IllegalArgumentException("Member Not found- Login Id: " +
            getAuthenticatedUser().getUserId() + " Room Id: " + roomId)).getChatRoomMemberId();
  }

  public User getAuthenticatedUser() {
    return userRepository.findByLoginId(CustomSecurityUtil.getCurrentUserLoginId())
        .orElseThrow(() -> new UserNotFoundException(CustomSecurityUtil.getCurrentUserLoginId()));
  }

  public List<ScheduleListResponse> getSchedules(Long roomId) {
    List<Schedule> list = scheduleRepository
        .findByChatRoom_ChatRoomIdOrderByStartAtDesc(roomId);

    return list.stream()
        .map(ScheduleListResponse::from)
        .toList();
  }

  public ScheduleResponse getScheduleDetail(Long scheduleId, Long memberId) {

    Schedule schedule = scheduleRepository.findById(scheduleId)
        .orElseThrow(() -> new RuntimeException("Schedule not found"));

    ParticipantStatus myStatus =
        participantRepository.findBySchedule_ScheduleIdAndChatRoomMember_ChatRoomMemberId(
                scheduleId, memberId
            )
            .map(ScheduleParticipant::getParticipantStatus)
            .orElse(ParticipantStatus.PENDING);

    int yes = participantRepository
        .countBySchedule_ScheduleIdAndParticipantStatus(scheduleId, ParticipantStatus.JOINED);

    int no = participantRepository
        .countBySchedule_ScheduleIdAndParticipantStatus(scheduleId, ParticipantStatus.DECLINED);

    boolean isCreator =
        schedule.getScheduleCreator().getChatRoomMemberId().equals(memberId);

    return ScheduleResponse.from(schedule, yes, no, myStatus, isCreator, memberId);
  }

}
