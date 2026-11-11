package com.recruitment.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类（第06步课件第四节）
 * 三个方法对应：生成 Token、验证 Token、从 Token 解析 userId。
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // 把 yml 中的密钥字符串转成 JJWT 签名需要的 SecretKey 对象
    // 生成 Token 和验证 Token 必须使用同一把密钥
    private SecretKey getSigningKey() {
        // 先按 UTF-8 转成字节，再生成 HMAC 签名密钥
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 第 1 个功能：登录成功后，按 userId 生成 Token
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId.toString())       // Token 里保存当前用户 ID
                .issuedAt(now)                    // 签发时间
                .expiration(expirationDate)       // 过期时间
                .signWith(getSigningKey())        // 用密钥签名，防止内容被篡改
                .compact();                       // 生成 Header.Payload.Signature 格式的最终 Token 字符串
    }

    /*
     * 下面两个方法都会调用 parseSignedClaims(token)，它会一次完成：
     * 1. 按「.」拆开 Header、Payload、Signature
     * 2. 解码 Header 和 Payload
     * 3. 使用 verifyWith 指定的密钥重新计算签名
     * 4. 比较新签名与 Token 自带的 Signature
     * 5. 检查 expiration 是否已经过期
     * 6. 验证成功后，返回包含 Payload 的 JWT 对象；失败则抛出异常
     */

    // 第 2 个功能：验证签名、格式、过期时间
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey()) // 指定验签密钥，必须与生成 Token 时相同
                    .build()                     // 创建 JWT 解析器
                    .parseSignedClaims(token);   // 参照上面注释
            return true;                         // 没有抛异常，说明 Token 有效
        } catch (Exception e) {
            return false;                        // 被篡改、过期或格式错误都会进入这里
        }
    }

    // 第 3 个功能：从有效 Token 中取回 userId
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey()) // 解析前仍要验签，不能直接相信 Payload
                    .build()                     // 创建 JWT 解析器
                    .parseSignedClaims(token)    // 参照上面注释
                    .getPayload();               // 取出 Payload（载荷）中的 Claims
            return Long.parseLong(
                    claims.getSubject());        // subject 中存的就是生成时放入的 userId
        } catch (Exception e) {
            return null;                         // Token 无效或 subject 不是数字
        }
    }
}
