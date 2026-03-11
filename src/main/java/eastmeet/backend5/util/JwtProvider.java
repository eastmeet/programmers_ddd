package eastmeet.backend5.util;

import static eastmeet.backend5.util.GlobalConstant.BASE_64_DECODER;
import static eastmeet.backend5.util.GlobalConstant.RSA_ALGORITHM;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
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

    public String generateAccessToken(Authentication authentication) {
        return this.generateToken(authentication, accessExpiration);
    }

    public String generateRefreshToken(Authentication authentication) {
        return this.generateToken(authentication, refreshExpiration);
    }

    private String generateToken(Authentication authentication, long expiration) {
        try {
            Date expireDate = new Date(System.currentTimeMillis() + expiration);

            return Jwts.builder().subject(String.valueOf(authentication.getPrincipal()))
                .expiration(expireDate)
                .signWith(loadPrivateKey(), SIG.RS256)
                .compact();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("JWT 토큰 생성 실패: {}", e.getMessage());
            return null;
        }
    }

    private PublicKey loadPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicBytes = BASE_64_DECODER.decode(jwtPublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        return KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(keySpec);
    }

    private PrivateKey loadPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateBytes = BASE_64_DECODER.decode(jwtPrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
        return KeyFactory.getInstance(RSA_ALGORITHM).generatePrivate(keySpec);
    }

}
