package runnity.service;

import jakarta.transaction.Transactional;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import runnity.domain.User;
import runnity.domain.UserMatchState;
import runnity.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String QUEUE_KEY = "match:queue:default";
    private static final String RESULT_KEY_FMT = "match:result:%s";

    @Transactional
    public void queueMatch(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getMatchState() == UserMatchState.MATCHED) throw new IllegalStateException("이미 매칭되었습니다.");
        if (user.getMatchState() == UserMatchState.SEARCHING) return;

        user.setMatchState(UserMatchState.SEARCHING);
        redisTemplate.opsForSet().add(QUEUE_KEY, String.valueOf(userId));
        userRepository.save(user);
    }

    @Transactional
    public void cancelMatch(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getMatchState() == UserMatchState.SEARCHING) {
            user.setMatchState(UserMatchState.IDLE);
            userRepository.save(user);
            redisTemplate.opsForSet().remove(QUEUE_KEY, String.valueOf(userId));
        }
    }

    public Map<String, Object> state(Long userId) {
        String result = redisTemplate.opsForValue().get(resultKey(userId));
        if (result != null) {
            return Map.of("state","MATCHED","chatRoomId", Long.valueOf(result));
        }
        Boolean queued = redisTemplate.opsForSet().isMember(QUEUE_KEY, String.valueOf(userId));
        return Map.of("state", (queued!=null && queued) ? "SEARCHING" : "IDLE");
    }

    private String resultKey(Long userId) {
        return String.format(RESULT_KEY_FMT, userId);
    }

}
