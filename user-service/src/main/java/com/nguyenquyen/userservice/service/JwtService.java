package com.nguyenquyen.userservice.service;

import com.nguyenquyen.userservice.dto.TokenDetails;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.Set;

public interface JwtService {
    String generateAccessToken(String userId, Set<String> roles);
    TokenDetails generateRefreshToken(String userId);
    SignedJWT validateToken(String token) throws ParseException, JOSEException;
}
