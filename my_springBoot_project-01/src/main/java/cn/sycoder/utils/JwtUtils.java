package cn.sycoder.utils;

import cn.sycoder.domain.SysUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    private static final String SECRET = "sycoder-jwt-secret-key-2026-hello";
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000L;

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USER_NAME = "userName";
    private static final String CLAIM_USER_REAL_NAME = "userRealName";

    private static final SecretKey SECRET_KEY =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private JwtUtils() {
    }

    /**
     * 生成 token
     */
    public static String generateToken(SysUser user) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + EXPIRE_TIME);

        return Jwts.builder()
                .issuedAt(now)
                .expiration(expireDate)
                .claim(CLAIM_USER_ID, user.getUserId())
                .claim(CLAIM_USER_NAME, user.getUserName())
                .claim(CLAIM_USER_REAL_NAME, user.getUserRealName())
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 校验 token
     */
    public static boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断 token 是否过期
     */
    public static boolean isExpired(String token) {
        try {
            Date expireTime = parseClaims(token).getExpiration();
            return expireTime.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 解析 Claims
     */
    public static Claims parseClaims(String token) {
        String realToken = removeBearerPrefix(token);
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(realToken)
                .getPayload();
    }

    /**
     * 逆向解析成 SysUser
     */
    public static SysUser parseToken(String token) {
        Claims claims = parseClaims(token);

        Map<String, Object> map = new HashMap<>();
        map.put("userId", claims.get(CLAIM_USER_ID));
        map.put("userName", claims.get(CLAIM_USER_NAME));
        map.put("userRealName", claims.get(CLAIM_USER_REAL_NAME));

        return JsonUtils.toObject(JsonUtils.toJson(map), SysUser.class);
    }

    private static String removeBearerPrefix(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new JwtException("token不能为空");
        }
        token = token.trim();
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}
