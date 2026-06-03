package com.nguyenquyen.userservice.service.impl;

import com.nguyenquyen.userservice.common.TokenType;
import com.nguyenquyen.userservice.dto.TokenDetails;
import com.nguyenquyen.userservice.exception.ErrorCode;
import com.nguyenquyen.userservice.exception.UserServiceException;
import com.nguyenquyen.userservice.service.JwtService;
import com.nguyenquyen.userservice.service.RedisTokenService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import static com.nguyenquyen.userservice.constant.JWTConstant.*;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret-key}")
    private String secretKey;

    private final RedisTokenService redisTokenService;

    @Override
    public String generateAccessToken(String userId, Set<String> roles) {
        // Header
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        Date issueTime = new Date();
        Date expiredTime = new Date(Instant.now().plus(2, ChronoUnit.HOURS).toEpochMilli());
        String jwtId = UUID.randomUUID().toString();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userId)
                .issuer(JWT_ISSUER)
                .claim(ROLES, roles)
                .issueTime(issueTime)
                .expirationTime(expiredTime)
                .jwtID(jwtId)
                .claim(TOKEN_TYPE, TokenType.ACCESS_TOKEN)
                .build();

        // Payload
        Payload payload = new Payload(claimsSet.toJSONObject());

        // Signature
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(secretKey));
        } catch (JOSEException e) {
            throw new UserServiceException(ErrorCode.TOKEN_GENERATION_FAILED);
        }
        return jwsObject.serialize();
    }

    @Override
    public TokenDetails generateRefreshToken(String userId) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        Date issueTime = new Date();
        Date expiredTime = new Date(Instant.now().plus(14, ChronoUnit.DAYS).toEpochMilli());
        long ttlSeconds = ChronoUnit.SECONDS.between(Instant.now(), expiredTime.toInstant());

        String jwtId = UUID.randomUUID().toString();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userId)
                .issuer(JWT_ISSUER)
                .issueTime(issueTime)
                .expirationTime(expiredTime)
                .claim(TOKEN_TYPE, TokenType.REFRESH_TOKEN)
                .jwtID(jwtId)
                .build();

        // Payload
        Payload payload = new Payload(claimsSet.toJSONObject());

        // Signature
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(secretKey));
        } catch (JOSEException e) {
            throw new UserServiceException(ErrorCode.TOKEN_GENERATION_FAILED);
        }
        String token = jwsObject.serialize();

        return TokenDetails.builder()
                .value(token)
                .jwtId(jwtId)
                .ttlSeconds(ttlSeconds)
                .build();
    }

    @Override
    public SignedJWT validateToken(String token) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
        if(expiration.before(new Date()))
            throw new UserServiceException(ErrorCode.TOKEN_EXPIRED);

        boolean verify = signedJWT.verify(new MACVerifier(secretKey));
        if(!verify)
            throw new UserServiceException(ErrorCode.TOKEN_INVALID);

        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        if(redisTokenService.existsByJwtId(jwtId))
            throw new UserServiceException(ErrorCode.TOKEN_INVALID);

        return signedJWT;
    }

}
