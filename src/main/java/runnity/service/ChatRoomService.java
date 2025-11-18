package runnity.service;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import runnity.domain.ChatRoom;
import runnity.domain.ChatRoomMember;
import runnity.domain.ChatRoomType;
import runnity.domain.Message;
import runnity.domain.MessageType;
import runnity.domain.User;
import runnity.domain.UserMatchState;
import runnity.dto.ChatMessageResponse;
import runnity.dto.ChatRoomRequest;
import runnity.dto.ChatRoomResponse;
import runnity.repository.ChatMessageRepository;
import runnity.repository.ChatRoomMemberRepository;
import runnity.repository.ChatRoomRepository;
import runnity.repository.UserRepository;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final S3Service s3Service;

    // 채팅방 owner 체크 (loginId 가져오기)
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
    public void editGroupChatRoom(Long chatRoomId, ChatRoomRequest request, MultipartFile newImage) throws IOException {
        User owner = userRepository.findById(request.getOwnerId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 입니다."));

        if (!room.getOwner().getUserId().equals(request.getOwnerId())) {
            throw new IllegalArgumentException("채팅방 수정 권한 없음.");
        }

        if (newImage == null || newImage.isEmpty()) {
            request.setImageUrl("/images/image-upload.png");
        } else {
            // profiles 는 나중에 S3 작업 다 하면 room_profiles 로 바꿀 예정
            String key = "profiles/" + chatRoomId + "/" + UUID.randomUUID();
            String newUrl = s3Service.upload(newImage, key);

            request.setImageUrl(newUrl);
        }

        room.editChatRoom(request, owner);
        if ((room.getImageUrl() != null) && (room.getImageUrl().equals("/images/image-upload.png"))) {
            String oldUrl = room.getImageUrl();
            String oldKey = oldUrl.substring(oldUrl.indexOf("profiles/"));
            s3Service.removeChatRoomProfileImage(oldKey);
        }
    }

    // 채팅방 생성 공통 메서드
    public ChatRoomResponse createChatRoom(ChatRoomRequest request, MultipartFile chatRoomImage) throws IOException {
        ChatRoom room;

        switch (request.getChatRoomType()) {
            case GROUP -> room = createGroupChatRoom(request, chatRoomImage);
            case DIRECT -> room = createDirectRoom(request.getMembers());
            default -> throw new IllegalArgumentException("알 수 없는 채팅방 TYPE 입니다.");
        }

        return ChatRoomResponse.from(room);
    }

    // 그룹 채팅방 생성 메서드
    public ChatRoom createGroupChatRoom(ChatRoomRequest request, MultipartFile chatRoomImage) throws IOException {
        User owner = userRepository.findById(request.getOwnerId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (chatRoomImage == null || chatRoomImage.isEmpty()) {
            request.setImageUrl("/images/image-upload.png");
        } else {
            String key = "profiles/" + owner.getUserId() + "/" + UUID.randomUUID();
            String fileUrl = s3Service.upload(chatRoomImage, key);
            request.setImageUrl(fileUrl);
        }

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

        Message joinMessage = Message.builder()
            .chatRoom(room)
            .senderId(user)
            .content(user.getNickname() + "님이 들어왔습니다.")
            .type(MessageType.SYSTEM_JOIN)
            .build();
        Message saved = chatMessageRepository.save(joinMessage);

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

        simpMessagingTemplate.convertAndSend("/topic/rooms." + chatRoomId,
            ChatMessageResponse.from(saved));

    }

    public boolean checkUserJoinedChatRoom(Long chatRoomId, Long userId) {
        return chatRoomRepository.existsByIdAndMembers_Id(chatRoomId, userId);
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
    @Transactional
    public void leaveRoom(Long chatRoomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 입니다."));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 입니다."));

        ChatRoomMember membership = chatRoomMemberRepository
            .findActiveByRoomAndUser(chatRoomId, userId)
            .orElseThrow(() -> new IllegalStateException("현재 방에 속해있지 않습니다."));

        membership.leaveGroupChatRoom();
        chatRoomMemberRepository.save(membership);

        int remaining = chatRoomMemberRepository.countActiveMembersByChatRoomId(chatRoomId);

        Message joinMessage = Message.builder()
            .chatRoom(room)
            .senderId(user)
            .content(user.getNickname() + "님이 나갔습니다.")
            .type(MessageType.SYSTEM_LEAVE)
            .build();
        Message saved = chatMessageRepository.save(joinMessage);

        simpMessagingTemplate.convertAndSend("/topic/rooms." + chatRoomId,
            ChatMessageResponse.from(saved));

        if ((room.getChatRoomType() != ChatRoomType.RANDOM && remaining == 0) || (room.getChatRoomType() == ChatRoomType.GROUP && room.getOwner().getUserId().equals(userId))) {
            chatRoomRepository.delete(room);
        } else if (room.getChatRoomType() == ChatRoomType.RANDOM && remaining == 1) {
            // RANDOM 매칭이면 매칭 신호 삭제
            redisTemplate.delete("match:result:" + userId);
            user.setMatchState(UserMatchState.IDLE);
            userRepository.save(user);
        } else if (room.getChatRoomType() == ChatRoomType.RANDOM && remaining == 0) {
            // RANDOM 매칭이면 매칭 신호 삭제
            redisTemplate.delete("match:result:" + userId);
            // 채팅방 유저의 Active 가 보두 left 일 때 삭제
            chatRoomRepository.delete(room);
            // 랜덤 채팅방 운동 완료 후 매칭 상태 복원
            user.setMatchState(UserMatchState.IDLE);
            userRepository.save(user);
        }
    }

}
