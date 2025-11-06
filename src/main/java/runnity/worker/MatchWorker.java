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

    private static final String QUEUE_KEY = "match:queue:default";
    private static final String USER_LOCK_KEY_FMT = "lock:user:%s";
    private static final String RESULT_KEY_FMT   = "match:result:%s"; // 추가
    private static final long LOCK_TTL_MS = 5_000;
    private static final long RESULT_TTL_SEC = 120; // 결과 신호 TTL (2분 정도)

    private static final String USER_NOTIFY_DEST = "/queue/notify";

    @Scheduled(fixedDelay = 300)
    public void run() {
        try {
            List<String> keys = Collections.singletonList(QUEUE_KEY);
            Object[] args = new Object[]{ String.valueOf(LOCK_TTL_MS) };

            @SuppressWarnings("unchecked")
            List<String> pair = (List<String>) redis.execute(duoMatchScript, keys, args);

            if (pair == null || pair.size() < 2) return;

            Long a = Long.valueOf(pair.get(0));
            Long b = Long.valueOf(pair.get(1));

            ChatRoom room = null;
            try {
                room = duoMatchService.createRoomAndBind(a, b);

                // 결과키 기록 (state가 MATCHED로 바뀌게 하는 신호)
                String roomIdStr = String.valueOf(room.getChatRoomId());
                redis.opsForValue().set(resultKey(a), roomIdStr, java.time.Duration.ofSeconds(RESULT_TTL_SEC));
                redis.opsForValue().set(resultKey(b), roomIdStr, java.time.Duration.ofSeconds(RESULT_TTL_SEC));

                // STOMP 알림은 유지
                Map<String, Object> payload = Map.of(
                    "type", "MATCHED",
                    "roomId", room.getChatRoomId(),
                    "roomType", String.valueOf(room.getChatRoomType())
                );
                // convertAndSendToUser의 첫 파라미터는 "Principal.getName()" 과 동일해야 배달됨
                messagingTemplate.convertAndSendToUser(String.valueOf(a), USER_NOTIFY_DEST, payload);
                messagingTemplate.convertAndSendToUser(String.valueOf(b), USER_NOTIFY_DEST, payload);

                log.info("Duo matched: users=({}, {}), roomId={}", a, b, room.getChatRoomId());

            } catch (Exception e) {
                log.warn("Match finalize failed for users=({}, {}): {}", a, b, e.toString());
                redis.opsForSet().add(QUEUE_KEY, String.valueOf(a), String.valueOf(b));
            } finally {
                redis.delete(lockKey(a));
                redis.delete(lockKey(b));
            }

        } catch (Exception outer) {
            log.error("MatchWorker iteration error", outer);
        }
    }

    private String lockKey(Long userId) { return String.format(USER_LOCK_KEY_FMT, userId); }
    private String resultKey(Long userId) { return String.format(RESULT_KEY_FMT, userId); } // ✅ 추가
}