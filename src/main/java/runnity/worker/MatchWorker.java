package runnity.worker;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import runnity.RunnerLevel;
import runnity.domain.ChatRoom;
import runnity.service.DuoMatchService;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchWorker {

    private final StringRedisTemplate redis;
    private final DefaultRedisScript<List> duoMatchScript;
    private final DuoMatchService duoMatchService;
    private final SimpMessagingTemplate messagingTemplate;

    // 유저 위치 3km 반경 내, 같은 RunLevel 조건 추가 전 코드
//    private static final String QUEUE_KEY = "match:queue:default";
//    private static final String USER_LOCK_KEY_FMT = "lock:user:%s";
//    private static final String RESULT_KEY_FMT   = "match:result:%s"; // 추가
    private static final long LOCK_TTL_MS = 5_000;
    private static final long RESULT_TTL_SEC = 120; // 결과 신호 TTL (2분 정도)
    private static final String USER_NOTIFY_DEST = "/queue/notify";

    private String waitKey(RunnerLevel lv) { return "match:wait:" + lv.name(); }
    private String geoKey (RunnerLevel lv) { return "match:geo:"  + lv.name(); }
    private String resultKey(Long userId)  { return "match:result:" + userId; }

    @Scheduled(fixedDelay = 300)
    public void run() {
        try {
            for (RunnerLevel level : RunnerLevel.values()) {
                String wKey = waitKey(level);
                String gKey = geoKey(level);

                @SuppressWarnings("unchecked")
                List<String> pair = (List<String>) redis.execute(
                    duoMatchScript,
                    java.util.Arrays.asList(wKey, gKey, "match:wait:idx" + level.name()),
                    new Object[]{ "3000", String.valueOf(LOCK_TTL_MS) }
                );

                if (pair == null || pair.size() < 2) {
                    continue;
                }

                Long a = Long.valueOf(pair.get(0));
                Long b = Long.valueOf(pair.get(1));

                ChatRoom room = null;
                try {
                    // 방 생성 및 유저 바인딩 (기존 서비스 재사용)
                    room = duoMatchService.createRoomAndBind(a, b);

                    String roomIdStr = String.valueOf(room.getChatRoomId());
                    // 결과 신호 기록 (MatchService.state 에서 확인)
                    redis.opsForValue().set(resultKey(a), roomIdStr, java.time.Duration.ofSeconds(RESULT_TTL_SEC));
                    redis.opsForValue().set(resultKey(b), roomIdStr, java.time.Duration.ofSeconds(RESULT_TTL_SEC));

                    // 유저별 STOMP 알림
                    Map<String, Object> payload = Map.of(
                        "type", "MATCHED",
                        "roomId", room.getChatRoomId(),
                        "roomType", String.valueOf(room.getChatRoomType())
                    );
                    messagingTemplate.convertAndSendToUser(String.valueOf(a), USER_NOTIFY_DEST, payload);
                    messagingTemplate.convertAndSendToUser(String.valueOf(b), USER_NOTIFY_DEST, payload);

                    log.info("Duo matched (level={}): users=({}, {}), roomId={}", level, a, b, room.getChatRoomId());

                } catch (Exception e) {
                    log.warn("Finalize failed (level={}, users=({}, {})): {}", level, a, b, e.toString());
                    // 실패 시 대기세트로 복귀 (지오는 클라/서비스 측에서 재등록 or 다음 큐잉 시 반영)
                    redis.opsForSet().add(wKey, String.valueOf(a), String.valueOf(b));
                } finally {
                    // 개별 락 정리 (스크립트에서 SET NX PX로 잡은 락 해제)
                    redis.delete("lock:user:" + a);
                    redis.delete("lock:user:" + b);
                }
            }
        } catch (Exception outer) {
            log.error("MatchWorker iteration error", outer);
        }
    }

    // 유저 위치 3km 반경 내, 같은 RunLevel 조건 추가 전 코드
//    @Scheduled(fixedDelay = 300)
//    public void run() {
//        try {
//            List<String> keys = Collections.singletonList(QUEUE_KEY);
//            Object[] args = new Object[]{ String.valueOf(LOCK_TTL_MS) };
//
//            @SuppressWarnings("unchecked")
//            List<String> pair = (List<String>) redis.execute(duoMatchScript, keys, args);
//
//            if (pair == null || pair.size() < 2) return;
//
//            Long a = Long.valueOf(pair.get(0));
//            Long b = Long.valueOf(pair.get(1));
//
//            ChatRoom room = null;
//            try {
//                room = duoMatchService.createRoomAndBind(a, b);
//
//                // 결과키 기록 (state가 MATCHED로 바뀌게 하는 신호)
//                String roomIdStr = String.valueOf(room.getChatRoomId());
//                redis.opsForValue().set(resultKey(a), roomIdStr, java.time.Duration.ofSeconds(RESULT_TTL_SEC));
//                redis.opsForValue().set(resultKey(b), roomIdStr, java.time.Duration.ofSeconds(RESULT_TTL_SEC));
//
//                // STOMP 알림은 유지
//                Map<String, Object> payload = Map.of(
//                    "type", "MATCHED",
//                    "roomId", room.getChatRoomId(),
//                    "roomType", String.valueOf(room.getChatRoomType())
//                );
//                // convertAndSendToUser의 첫 파라미터는 "Principal.getName()" 과 동일해야 배달됨
//                messagingTemplate.convertAndSendToUser(String.valueOf(a), USER_NOTIFY_DEST, payload);
//                messagingTemplate.convertAndSendToUser(String.valueOf(b), USER_NOTIFY_DEST, payload);
//
//                log.info("Duo matched: users=({}, {}), roomId={}", a, b, room.getChatRoomId());
//
//            } catch (Exception e) {
//                log.warn("Match finalize failed for users=({}, {}): {}", a, b, e.toString());
//                redis.opsForSet().add(QUEUE_KEY, String.valueOf(a), String.valueOf(b));
//            } finally {
//                redis.delete(lockKey(a));
//                redis.delete(lockKey(b));
//            }
//
//        } catch (Exception outer) {
//            log.error("MatchWorker iteration error", outer);
//        }
//    }
//
//    private String lockKey(Long userId) { return String.format(USER_LOCK_KEY_FMT, userId); }
//    private String resultKey(Long userId) { return String.format(RESULT_KEY_FMT, userId); }
}