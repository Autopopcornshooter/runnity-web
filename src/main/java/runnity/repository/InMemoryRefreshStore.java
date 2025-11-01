package runnity.repository;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryRefreshStore {

  private final Map<String, Entry> store = new ConcurrentHashMap<>();

  public void save(String jti, String username, Instant expiresAt) {
    store.put(jti, new Entry(username, expiresAt, false));
  }

  public Entry get(String jti) {
    return store.get(jti);
  }

  public void revoke(String jti) {
    store.computeIfPresent(jti, (k, v) -> new Entry(v.username(), v.expiresAt(), true));
  }

  public void rotate(String oldJti, String newJti, String username, Instant expiresAt) {
    revoke(oldJti);
    save(newJti, username, expiresAt);
  }

  public record Entry(String username, Instant expiresAt, boolean revoked) {

  }
}
