package runnity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import runnity.domain.ChatRoomMember;
import runnity.domain.ParticipantStatus;
import runnity.domain.Schedule;
import runnity.domain.ScheduleParticipant;
import runnity.dto.ParticipantRequest;
import runnity.dto.ParticipantResponse;
import runnity.repository.ChatRoomMemberRepository;
import runnity.repository.ScheduleParticipantRepository;
import runnity.repository.ScheduleRepository;

@Service
@RequiredArgsConstructor
public class ScheduleParticipantService {

  private final ScheduleParticipantRepository participantRepository;
  private final ScheduleRepository scheduleRepository;
  private final ChatRoomMemberRepository memberRepository;

  @Transactional
  public ParticipantResponse save(ParticipantRequest req) {

    // 1) 채팅방 멤버 조회
    ChatRoomMember member = memberRepository.findById(req.getMemberId())
        .orElseThrow(() -> new IllegalArgumentException("ChatRoomMember not found"));

    // 2) 스케줄 조회
    Schedule schedule = scheduleRepository.findById(req.getScheduleId())
        .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));

    // 3) 기존 참여 데이터 조회
    ScheduleParticipant participant = participantRepository
        .findBySchedule_ScheduleIdAndChatRoomMember_ChatRoomMemberId(
            req.getScheduleId(), req.getMemberId()
        )
        .orElseGet(() -> ScheduleParticipant.builder()
            .schedule(schedule)
            .chatRoomMember(member)
            .participantStatus(ParticipantStatus.PENDING)
            .build()
        );

    // 4) 상태 업데이트
    participant.updateStatus(req.getParticipantStatus()); // 엔티티에 setter 대신 update 메서드 필요

    participantRepository.save(participant);

    // 5) 통계 계산
    int yes = participantRepository.countBySchedule_ScheduleIdAndParticipantStatus(
        req.getScheduleId(), ParticipantStatus.JOINED);

    int no = participantRepository.countBySchedule_ScheduleIdAndParticipantStatus(
        req.getScheduleId(), ParticipantStatus.DECLINED);

    // 6) 응답
    return ParticipantResponse.builder()
        .participantStatus(participant.getParticipantStatus())
        .memberId(req.getMemberId())
        .scheduleId(req.getScheduleId())
        .yesCount(yes)
        .noCount(no)
        .build();
  }
}

