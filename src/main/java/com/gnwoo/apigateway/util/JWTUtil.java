package com.gnwoo.apigateway.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTUtil {
    private final String secret = "secret";
    private final Algorithm algorithm = Algorithm.HMAC256(secret);
    private final String issuer = "gnw-auth-service";

    public JWTUtil() {}

    public String consturctJWT (Long uuid) {
        String JWT_token;
        JWT_token = JWT.create()
                       .withIssuer(issuer)
                       .withClaim("uuid", uuid)
                       .withIssuedAt(new Date())
                       .sign(algorithm);
        return JWT_token;
    }

    public boolean verifyJWT(Long uuid, String JWT_token) {
        try
        {
            JWTVerifier verifier = JWT.require(algorithm)
                                      .withIssuer(issuer)
                                      .withClaim("uuid", uuid)
                                      .build();
            DecodedJWT decoded_JWT_token = verifier.verify(JWT_token);
            return true;
        }
        catch (JWTVerificationException exception)
        {
            // Invalid signature/claims
            return false;
        }
    }

    public String extract_JWT_signature(String JWT_token)
    {
        int dot_cnt = 0;
        for(int i = 0; i < JWT_token.length(); i++)
        {
            if(JWT_token.charAt(i) == '.')
            {
                dot_cnt += 1;
                if(dot_cnt == 2)
                    return JWT_token.substring(i+1);
            }
        }
        return null;
    }
}
