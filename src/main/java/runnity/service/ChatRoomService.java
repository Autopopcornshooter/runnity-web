package runnity.service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import runnity.domain.ChatRoom;
import runnity.domain.ChatRoomMember;
import runnity.domain.ChatRoomType;
import runnity.domain.User;
import runnity.domain.UserMatchState;
import runnity.dto.ChatRoomRequest;
import runnity.dto.ChatRoomResponse;
import runnity.repository.ChatRoomMemberRepository;
import runnity.repository.ChatRoomRepository;
import runnity.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;

    // 채팅방 owner 체크 (nickname 가져오기)
    public String getLoginId(Long ownerId) {
        return userRepository.findById(ownerId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."))
            .getLoginId();
    }

    // 채팅방 owner 체크 (userId 가져오기)
    public Long getCheckUserId(String loginId) {
        return userRepository.findByLoginId(loginId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."))
            .getUserId();
    }

    public ChatRoomResponse findById(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 입니다."));

        return ChatRoomResponse.from(chatRoom);
    }

    // 자신이 속해 있는 채팅방 목록
    public List<ChatRoomResponse> getUserChatRoom(Long userId) {

        List<ChatRoom> userChatRooms = chatRoomMemberRepository.findChatRoomsByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 입니다."));

        return userChatRooms.stream()
            .map(r -> ChatRoomResponse.from(r))
            .toList();
    }

    // 채팅방 전체 목록
    public List<ChatRoomResponse> getAllChatRoom() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();

        return chatRooms.stream()
            .map(r -> ChatRoomResponse.from(r))
            .toList();
    }

    // GROUP 채팅방 전체 목록
    public List<ChatRoomResponse> getAllGroupChatRoom() {
        List<ChatRoom> chatRooms = chatRoomRepository.findByChatRoomType(ChatRoomType.GROUP);

        return chatRooms.stream()
            .map(r -> ChatRoomResponse.from(r))
            .toList();
    }

    // 나의 채팅방 전체 목록
    public List<ChatRoomResponse> getMyChatRoom(Long userId) {
        List<ChatRoom> chatRooms = chatRoomMemberRepository.findChatRoomsByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return chatRooms.stream()
            .map(r -> ChatRoomResponse.from(r))
            .toList();
    }

    @Transactional
    public void editGroupChatRoom(Long chatRoomId, ChatRoomRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 입니다."));

        System.out.println(room.getOwner().getUserId());
        System.out.println(request.getOwnerId());

        if (!room.getOwner().getUserId().equals(request.getOwnerId())) {
            throw new IllegalArgumentException("채팅방 수정 권한 없음.");
        }

        room.editChatRoom(request, owner);
    }

    // 채팅방 생성 공통 메서드
    public ChatRoomResponse createChatRoom(ChatRoomRequest request) {
        ChatRoom room;

        switch (request.getChatRoomType()) {
            case GROUP -> room = createGroupChatRoom(request);
            case DIRECT -> room = createDirectRoom(request.getMembers());
            case RANDOM -> room = createRandomChatRoom();
            default -> throw new IllegalArgumentException("알 수 없는 채팅방 TYPE 입니다.");
        }

        return ChatRoomResponse.from(room);
    }

    // 그룹 채팅방 생성 메서드
    public ChatRoom createGroupChatRoom(ChatRoomRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        ChatRoom room = ChatRoom.builder()
            .chatRoomType(ChatRoomType.GROUP)
            .chatRoomName(request.getChatRoomName())
            .description(request.getDescription())
            .region(request.getRegion())
            .imageUrl(request.getImageUrl())
            .owner(owner)
            .build();

        chatRoomRepository.save(room);

        ChatRoomMember member = ChatRoomMember.builder()
            .chatRoom(room)
            .user(owner)
            .build();

        room.addMember(member);
        chatRoomMemberRepository.save(member);

        return room;
    }

    // 채팅방 JOIN 메서드
    @Transactional
    public void joinGroupChatRoom(Long chatRoomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 입니다."));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 입니다."));

        Optional<ChatRoomMember> activeOpt = chatRoomMemberRepository.findActiveByRoomAndUser(chatRoomId, userId);
        if (activeOpt.isPresent()) {
            return;
        }

        Optional<ChatRoomMember> anyMembership = chatRoomMemberRepository.findByRoomAndUser(chatRoomId, userId);

        if (anyMembership.isPresent()) {
            ChatRoomMember member = anyMembership.get();
            member.joinGroupChatRoom();
            chatRoomMemberRepository.save(member);
        } else {
            ChatRoomMember member = ChatRoomMember.builder()
                .chatRoom(room)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .build();
            member.joinGroupChatRoom();
            room.addMember(member);
            chatRoomRepository.save(room);
        }

    }

    // 1:1 채팅방 생성 메서드 (미완성)
    public ChatRoom createDirectRoom(List<Long> userIds) {
        if (userIds == null || userIds.size() != 2)
            throw new IllegalArgumentException("1:1 채팅은 정확히 2명의 userId가 필요합니다.");

        ChatRoom room = ChatRoom.builder()
            .chatRoomType(ChatRoomType.DIRECT)
            .build();
        chatRoomRepository.save(room);

        addMembers(room, userIds);
        return room;
    }

    // RANDOM 채팅방 생성 메서드 (미완성)
    public ChatRoom createRandomChatRoom() {
        ChatRoom room = ChatRoom.builder()
            .chatRoomType(ChatRoomType.RANDOM)
            .build();
        chatRoomRepository.save(room);
        return room;
    }

    // 1:1 채팅방이랑 RANDOM 채팅방에 사용할지? 아직 모르겠음..
    public void addMembers(ChatRoom chatRoom, List<Long> members) {
        for (Long userId : members) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 입니다." + userId));

            boolean exists = chatRoom.getMembers().stream()
                .anyMatch(m -> m.getUser().getUserId().equals(user.getUserId()));

            if (exists) continue;

            ChatRoomMember member = ChatRoomMember.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();

            chatRoomMemberRepository.save(member);
            chatRoom.addMember(member);
        }
    }

    // 채팅방 나가기 메서드 & 운동완료 메서드
    public void leaveRoom(Long chatRoomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 입니다."));

        ChatRoomMember membership = chatRoomMemberRepository
            .findActiveByRoomAndUser(chatRoomId, userId)
            .orElseThrow(() -> new IllegalStateException("현재 방에 속해있지 않습니다."));

        membership.leaveGroupChatRoom();
        chatRoomMemberRepository.save(membership);

        int remaining = chatRoomMemberRepository.countActiveMembersByChatRoomId(chatRoomId);

        if ((room.getChatRoomType() != ChatRoomType.RANDOM && remaining == 0) || (room.getChatRoomType() == ChatRoomType.GROUP && room.getOwner().getUserId().equals(userId))) {
            chatRoomRepository.delete(room);
        } else if (room.getChatRoomType() == ChatRoomType.RANDOM && remaining == 1) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
            user.setMatchState(UserMatchState.IDLE);
            userRepository.save(user);
        } else if (room.getChatRoomType() == ChatRoomType.RANDOM && remaining == 0) {
            // 채팅방 유저의 Active 가 보두 left 일 때 삭제
            chatRoomRepository.delete(room);
            // 랜덤 채팅방 운동 완료 후 매칭 상태 복원
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
            user.setMatchState(UserMatchState.IDLE);
            userRepository.save(user);
        }
    }

    // RANDOM 채팅에서 운동 종료 버튼 누르면 채팅방 사라지는 메서드
    public void finishRandomRoom(Long chatRoomId) {
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 입니다."));

        if (room.getChatRoomType() != ChatRoomType.RANDOM) {
            throw new IllegalArgumentException("운동 종료는 RANDOM 채팅방에서만 가능합니다.");
        }

        chatRoomMemberRepository.deleteAllByChatRoomId(chatRoomId);
        chatRoomRepository.delete(room);
    }



}
