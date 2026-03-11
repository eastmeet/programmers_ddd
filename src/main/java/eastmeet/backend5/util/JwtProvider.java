package eastmeet.backend5.util;

import static eastmeet.backend5.util.GlobalConstant.BASE_64_DECODER;
import static eastmeet.backend5.util.GlobalConstant.RSA_ALGORITHM;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.expiration.access}")
    private long accessExpiration;

    @Value("${jwt.expiration.access}")
    private long refreshExpiration;

    @Value("${jwt.key.public}")
    private String jwtPublicKey;

    @Value("${jwt.key.private}")
    private String jwtPrivateKey;

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @PostConstruct
    public void init() {
        this.publicKey = loadPublicKey();
        this.privateKey = loadPrivateKey();
    }

    public String generateAccessToken(Authentication authentication) {
        return this.generateToken(authentication, accessExpiration);
    }

    public String generateRefreshToken(Authentication authentication) {
        return this.generateToken(authentication, refreshExpiration);
    }

    private String generateToken(Authentication authentication, long expiration) {
        Date expireDate = new Date(System.currentTimeMillis() + expiration);

        return Jwts.builder().subject(String.valueOf(authentication.getPrincipal()))
            .expiration(expireDate)
            .signWith(privateKey, SIG.RS256)
            .compact();
    }

    public Jws<Claims> validateToken(String token) {
        try {
            return Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            log.error("만료된 토큰: {}", e.getMessage());
            throw new IllegalArgumentException("만료된 토큰입니다.", e);
        } catch (SignatureException e) {
            log.error("서명 검증 실패: {}", e.getMessage());
            throw new IllegalArgumentException("서명이 유효하지 않은 토큰입니다.", e);
        } catch (MalformedJwtException e) {
            log.error("잘못된 토큰 형식: {}", e.getMessage());
            throw new IllegalArgumentException("잘못된 형식의 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 토큰: {}", e.getMessage());
            throw new IllegalArgumentException("지원하지 않는 토큰입니다.", e);
        } catch (JwtException e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.", e);
        }
    }

    private PublicKey loadPublicKey() {
        try {
            byte[] publicBytes = BASE_64_DECODER.decode(jwtPublicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            return KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("공개키 생성 실패: {}", e.getMessage());
            throw new IllegalStateException("공개키 생성 실패", e);
        }
    }

    private PrivateKey loadPrivateKey() {
        try {
            byte[] privateBytes = BASE_64_DECODER.decode(jwtPrivateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
            return KeyFactory.getInstance(RSA_ALGORITHM).generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("개인키 생성 실패: {}", e.getMessage());
            throw new IllegalStateException("개인키 생성 실패", e);
        }
    }

}
