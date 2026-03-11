package eastmeet.backend5.util;

import java.util.Base64;

public class GlobalConstant {

    public static final Base64.Encoder BASE_64_ENCODER = Base64.getEncoder();

    public static final Base64.Decoder BASE_64_DECODER = Base64.getDecoder();

    public static final String REFRESH_TOKEN_HEADER = "Refresh-Token";

    public static final String RSA_ALGORITHM = "RSA";

}
