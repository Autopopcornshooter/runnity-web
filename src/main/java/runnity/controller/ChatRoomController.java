package runnity.controller;

import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import runnity.dto.ChatRoomRequest;
import runnity.dto.ChatRoomResponse;
import runnity.service.ChatRoomService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatRoomResponse> createGroupChatRoom(@ModelAttribute ChatRoomRequest request, @RequestParam(name = "imageUrl", required = false) MultipartFile imageUrl, Principal principal) {
        String loginId = principal.getName();
        Long userId = chatRoomService.getCheckUserId(loginId);
        request.setOwnerId(userId);
        if (imageUrl == null || imageUrl.isEmpty()) {
            request.setImageUrl("/images/image-upload.png");
        } else {
            // 나중에 S3 추가되면 적용
            // String fileUrl = s3Service.uploadFile(imageUrl);
            // System.out.println("저장된 이미지 URL : " + fileUrl);
            // request.setImageUrl(fileUrl);
            // return ResponseEntity.status(HttpStatus.CREATED).body(chatRoomResponse);
            String testFileName = imageUrl.getOriginalFilename();
            request.setImageUrl(testFileName);
        }

        ChatRoomResponse chatRoomResponse = chatRoomService.createChatRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(chatRoomResponse);
    }

    @PutMapping("/{chatRoomId}")
    public ResponseEntity<String> editGroupChatRoom(@RequestBody ChatRoomRequest request, @PathVariable Long chatRoomId, Principal principal) {
        String loginId = principal.getName();
        Long userId = chatRoomService.getCheckUserId(loginId);
        request.setOwnerId(userId);
        chatRoomService.editGroupChatRoom(chatRoomId, request);
        return ResponseEntity.status(HttpStatus.OK).body("채팅방이 수정되었습니다.");
    }

    // 그룹 채팅방 JOIN
    @PostMapping("/{chatRoomId}/join")
    public ResponseEntity<String> joinGroupChatRoom(@PathVariable Long chatRoomId, Principal principal) {
        String loginId = principal.getName();
        Long userId = chatRoomService.getCheckUserId(loginId);
        chatRoomService.joinGroupChatRoom(chatRoomId, userId);
        return ResponseEntity.ok("그룹 채팅방 JOIN 성공");
    }

    // 그룹 채팅방 나가기
    @DeleteMapping("/{chatRoomId}/leave")
    public ResponseEntity<String> leaveGroupChatRoom(@PathVariable Long chatRoomId, Principal principal) {
        String loginId = principal.getName();
        Long userId = chatRoomService.getCheckUserId(loginId);
        chatRoomService.leaveRoom(chatRoomId, userId);
        return ResponseEntity.ok("그룹 채팅방 나가기 성공");
    }

    // 채팅방 전체 목록
    @GetMapping
    public ResponseEntity<List<ChatRoomResponse>> getAllChatRooms() {
        List<ChatRoomResponse> list = chatRoomService.getAllChatRoom();
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    // 속해 있는 채팅방 목록
    @GetMapping("/{userId}")
    public ResponseEntity<List<ChatRoomResponse>> getUserChatRoom(@PathVariable Long userId) {
        List<ChatRoomResponse> list = chatRoomService.getUserChatRoom(userId);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

}
