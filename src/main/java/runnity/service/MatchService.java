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

    private static final String RESULT_KEY_FMT = "match:result:%s";
    private static String waitKey(RunnerLevel lv) { return "match:wait:" + lv.name(); }
    private static String geoKey (RunnerLevel lv) { return "match:geo:"  + lv.name(); }
    private static String waitIdxKey (RunnerLevel lv) { return "match:wait:idx:"  + lv.name(); }

    @Transactional
    public void queueMatch(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getMatchState() == UserMatchState.MATCHED) throw new IllegalStateException("이미 매칭되었습니다.");

        String wKey = waitKey(user.getRunnerLevel());
        String gKey = geoKey(user.getRunnerLevel());
        String idxKey = waitIdxKey(user.getRunnerLevel());

        // 이미 SEARCHING인데 꼬였을 가능성 => Redis 쪽을 리셋하고 다시 넣는다
        if (user.getMatchState() == UserMatchState.SEARCHING) {
            redisTemplate.opsForSet().remove(wKey, String.valueOf(userId));
            redisTemplate.opsForZSet().remove(idxKey, String.valueOf(userId));
            redisTemplate.opsForZSet().remove(gKey, String.valueOf(userId));
        }

        user.setMatchState(UserMatchState.SEARCHING);
        userRepository.save(user);

        // 매칭 먼저 잡은 순으로
        long now = System.currentTimeMillis();
        redisTemplate.opsForSet().add(wKey, String.valueOf(userId));
        redisTemplate.opsForZSet().add(idxKey, String.valueOf(userId), now);
        pushGeo(user, gKey, String.valueOf(userId));
    }

    @Transactional
    public void cancelMatch(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getMatchState() == UserMatchState.SEARCHING) {
            user.setMatchState(UserMatchState.IDLE);
            userRepository.save(user);

            String wKey = waitKey(user.getRunnerLevel());
            String gKey = geoKey(user.getRunnerLevel());
            String idxKey = waitIdxKey(user.getRunnerLevel());

            redisTemplate.opsForSet().remove(wKey, String.valueOf(userId));
            redisTemplate.opsForZSet().remove(idxKey, String.valueOf(userId));
            redisTemplate.opsForZSet().remove(gKey, String.valueOf(userId));
        }
    }

    public Map<String, Object> state(Long userId) {
        String result = redisTemplate.opsForValue().get(resultKey(userId));
        if (result != null) {
            return Map.of("state","MATCHED","chatRoomId", Long.valueOf(result));
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        RunnerLevel level = user.getRunnerLevel();
        String uid = String.valueOf(userId);

        String wKey = waitKey(level);
        String gKey = geoKey(level);
        String idxKey = waitIdxKey(user.getRunnerLevel());

        Boolean queued = redisTemplate.opsForSet().isMember(wKey, uid);

        // queue 에 데이터가 쌓이지 않아서 추가해준 복구 로직
        if ((queued == null || !queued) && user.getMatchState() == UserMatchState.SEARCHING) {
            // Redis 쪽 상태를 한 번 깨끗하게 정리하고
            redisTemplate.opsForSet().remove(wKey, uid);
            redisTemplate.opsForZSet().remove(idxKey, uid);
            redisTemplate.opsForZSet().remove(gKey, uid);

            // 다시 제대로 큐에 넣어준다.
            long now = System.currentTimeMillis();
            redisTemplate.opsForSet().add(wKey, uid);
            redisTemplate.opsForZSet().add(idxKey, uid, now);

            // 유저 위치가 꼭 있어야 한다면, null 체크 한 번 정도 해줘도 좋음
            if (user.getRegion() != null) {
                pushGeo(user, gKey, uid);
            }

            // 프론트 입장에서는 여전히 "SEARCHING"
            return Map.of("state", "SEARCHING");
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
