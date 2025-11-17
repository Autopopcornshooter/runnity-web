package runnity.service;

import jakarta.transaction.Transactional;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import runnity.RunnerLevel;
import runnity.domain.User;
import runnity.domain.UserMatchState;
import runnity.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;

    // private static final String QUEUE_KEY = "match:queue:default";
    private static final String RESULT_KEY_FMT = "match:result:%s";
    private static String waitKey(RunnerLevel lv) { return "match:wait:" + lv.name(); }
    private static String geoKey (RunnerLevel lv) { return "match:geo:"  + lv.name(); }

    @Transactional
    public void queueMatch(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getMatchState() == UserMatchState.MATCHED) throw new IllegalStateException("이미 매칭되었습니다.");
        if (user.getMatchState() == UserMatchState.SEARCHING) return;

        // 유저 위치 3km 반경 내, 같은 RunLevel 조건 추가 전 코드
//        user.setMatchState(UserMatchState.SEARCHING);
//        redisTemplate.opsForSet().add(QUEUE_KEY, String.valueOf(userId));
//        userRepository.save(user);

        user.setMatchState(UserMatchState.SEARCHING);
        userRepository.save(user);

        String wKey = waitKey(user.getRunnerLevel());
        String gKey = geoKey(user.getRunnerLevel());

        // 매칭 먼저 잡은 순으로
        long now = System.currentTimeMillis();
        redisTemplate.opsForSet().add(wKey, String.valueOf(userId));
        redisTemplate.opsForZSet().add("match:wait:idx" + user.getRunnerLevel().name(), String.valueOf(userId), now);
        pushGeo(user, gKey, String.valueOf(userId));
    }

    @Transactional
    public void cancelMatch(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 유저 위치 3km 반경 내, 같은 RunLevel 조건 추가 전 코드
//        if (user.getMatchState() == UserMatchState.SEARCHING) {
//            user.setMatchState(UserMatchState.IDLE);
//            userRepository.save(user);
//            redisTemplate.opsForSet().remove(QUEUE_KEY, String.valueOf(userId));
//        }

        if (user.getMatchState() == UserMatchState.SEARCHING) {
            user.setMatchState(UserMatchState.IDLE);
            userRepository.save(user);

            String wKey = waitKey(user.getRunnerLevel());
            String gKey = geoKey(user.getRunnerLevel());

            redisTemplate.opsForSet().remove(wKey, String.valueOf(userId));
            // 매칭 대기순 제거
            redisTemplate.opsForZSet().remove("match:wait:idx" + user.getRunnerLevel().name(), String.valueOf(userId));
            // GEO 정리(안 해도 Lua가 매칭 시 ZREM 하지만, 취소면 여기서 제거)
            redisTemplate.opsForZSet().remove(gKey, String.valueOf(userId));
        }
    }

    public Map<String, Object> state(Long userId) {
        String result = redisTemplate.opsForValue().get(resultKey(userId));
        if (result != null) {
            return Map.of("state","MATCHED","chatRoomId", Long.valueOf(result));
        }

        // 유저 위치 3km 반경 내, 같은 RunLevel 조건 추가 전 코드
//        Boolean queued = redisTemplate.opsForSet().isMember(QUEUE_KEY, String.valueOf(userId));
//        return Map.of("state", (queued!=null && queued) ? "SEARCHING" : "IDLE");

        RunnerLevel level = userRepository.findById(userId)
            .map(u -> u.getRunnerLevel())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Boolean queued = redisTemplate.opsForSet()
            .isMember(waitKey(level), String.valueOf(userId));

        if (queued == null || !queued) {
            UserMatchState state = userRepository.findById(userId)
                .map(u -> u.getMatchState())
                .orElse(UserMatchState.IDLE);
            if (state == UserMatchState.SEARCHING) {
                return Map.of("state", "SEARCHING");
            }
        }

        return Map.of("state", (queued != null && queued) ? "SEARCHING" : "IDLE");
    }

    private String resultKey(Long userId) {
        return String.format(RESULT_KEY_FMT, userId);
    }

    private void pushGeo(User user, String gKey, String userId) {
        if (user.getRegion() == null) { throw new IllegalStateException("유저의 지역 정보가 없습니다."); }

        double lon = user.getRegion().getLng();
        double lat = user.getRegion().getLat();

        // 고수준 API는 connection 명시 안해줘도 된다고 함.
        // RedisConnection connection = redisTemplate.getRequiredConnectionFactory().getConnection();

        RedisGeoCommands.GeoLocation<String> location =
            new RedisGeoCommands.GeoLocation<>(String.valueOf(userId), new Point(lon, lat));

        redisTemplate.opsForGeo().add(gKey, location);
    }

}
