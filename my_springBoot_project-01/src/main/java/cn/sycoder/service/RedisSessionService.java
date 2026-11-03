package cn.sycoder.service;

import cn.sycoder.auth.AuthInfo;
import cn.sycoder.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.TimeUnit;

@Service
public class RedisSessionService {

    private static final String LOGIN_TOKEN_KEY_PREFIX = "login:token:";
    private static final long TOKEN_TTL_SECONDS = TimeUnit.HOURS.toSeconds(24);

    private final JedisPool jedisPool;

    public RedisSessionService(@Value("${app.redis.host:127.0.0.1}") String host,
                               @Value("${app.redis.port:6379}") int port) {
        this.jedisPool = new JedisPool(host, port);
    }

    public void saveAuthInfo(String token, AuthInfo authInfo) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(buildTokenKey(token), (int) TOKEN_TTL_SECONDS, JsonUtils.toJson(authInfo));
        }
    }

    public AuthInfo getAuthInfo(String token) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.get(buildTokenKey(token));
            if (json == null || json.trim().isEmpty()) {
                return null;
            }
            return JsonUtils.toObject(json, AuthInfo.class);
        }
    }

    public void refreshAuthInfo(String token) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.expire(buildTokenKey(token), (int) TOKEN_TTL_SECONDS);
        }
    }

    public void deleteAuthInfo(String token) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(buildTokenKey(token));
        }
    }

    public long getTokenTtlMillis() {
        return TimeUnit.SECONDS.toMillis(TOKEN_TTL_SECONDS);
    }

    private String buildTokenKey(String token) {
        return LOGIN_TOKEN_KEY_PREFIX + token;
    }
}
