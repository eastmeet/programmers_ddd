package eastmeet.backend5.key;

import static eastmeet.backend5.util.GlobalConstant.BASE_64_DECODER;
import static eastmeet.backend5.util.GlobalConstant.BASE_64_ENCODER;
import static eastmeet.backend5.util.GlobalConstant.RSA_ALGORITHM;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@Slf4j
class JwtProviderTest {

    private String base64PrivateKey;

    @BeforeEach
    void makeKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator rsa = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        KeyPair keyPair = rsa.genKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        BASE_64_ENCODER.encodeToString(publicKey.getEncoded());

        PrivateKey privateKey1 = keyPair.getPrivate();
        base64PrivateKey = BASE_64_ENCODER.encodeToString(privateKey1.getEncoded());
    }

    @Test
    void generateAccessToken() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Date expireDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60);
        Authentication authentication = new UsernamePasswordAuthenticationToken(UUID.randomUUID(), null);

        byte[] privateBytes = BASE_64_DECODER.decode(base64PrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
        PrivateKey privateKey = KeyFactory.getInstance(RSA_ALGORITHM).generatePrivate(keySpec);

        Jwts.builder().subject(String.valueOf(authentication.getPrincipal()))
            .expiration(expireDate)
            .signWith(privateKey, SIG.RS256)
            .compact();
    }

}
