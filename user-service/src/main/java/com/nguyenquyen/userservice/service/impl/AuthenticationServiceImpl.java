package com.nguyenquyen.userservice.service.impl;

import com.nguyenquyen.userservice.dto.TokenDetails;
import com.nguyenquyen.userservice.dto.request.IntrospectRequest;
import com.nguyenquyen.userservice.dto.request.LoginRequest;
import com.nguyenquyen.userservice.dto.response.IntrospectResponse;
import com.nguyenquyen.userservice.dto.response.LoginResponse;
import com.nguyenquyen.userservice.entity.RedisToken;
import com.nguyenquyen.userservice.entity.User;
import com.nguyenquyen.userservice.exception.ErrorCode;
import com.nguyenquyen.userservice.exception.UserServiceException;
import com.nguyenquyen.userservice.repository.UserRepository;
import com.nguyenquyen.userservice.service.AuthenticationService;
import com.nguyenquyen.userservice.service.JwtService;
import com.nguyenquyen.userservice.service.RedisTokenService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTHENTICATION-SERVICE")
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RedisTokenService redisTokenService;

    @Override
    public LoginResponse login(LoginRequest request) {
        String email = request.email();
        String password = request.password();

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        User user = (User) authenticate.getPrincipal();
        if(user == null) {
            throw new UserServiceException(ErrorCode.USER_NOT_FOUND);
        }

        Set<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        String accessToken = jwtService.generateAccessToken(user.getId(), roles);
        TokenDetails refreshToken = jwtService.generateRefreshToken(user.getId());

        RedisToken redisToken = RedisToken.builder()
                .jwtId(refreshToken.jwtId())
                .userId(user.getId())
                .expiration(refreshToken.ttlSeconds())
                .build();

        redisTokenService.saveToken(redisToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.value())
                .roles(roles)
                .build();
    }

    @Override
    public LoginResponse refreshToken(String refreshToken)  {
        try {
            SignedJWT signedJWT = jwtService.validateToken(refreshToken);
            String userId = signedJWT.getJWTClaimsSet().getSubject();

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserServiceException(ErrorCode.USER_NOT_FOUND));

            Set<String> roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            String newAccessToken = jwtService.generateAccessToken(userId, roles);

            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .roles(roles)
                    .build();

        } catch (ParseException | JOSEException e) {
            throw new UserServiceException(ErrorCode.TOKEN_INVALID);
        }
    }

    @Override
    public void logout(String refreshToken) throws ParseException, JOSEException {
        if (refreshToken == null) {
            throw new UserServiceException(ErrorCode.MISSING_LOGOUT_INFO);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null)
            throw new UserServiceException(ErrorCode.TOKEN_INVALID);
        String userId = authentication.getName();

        SignedJWT signedRefreshToken = jwtService.validateToken(refreshToken);

        String refreshUserId = signedRefreshToken.getJWTClaimsSet().getSubject();
        String refreshJwtId = signedRefreshToken.getJWTClaimsSet().getJWTID();

        if (!userId.equals(refreshUserId)) {
            throw new UserServiceException(ErrorCode.TOKEN_INVALID);
        }

        redisTokenService.deleteTokenByJwtId(refreshJwtId);

        Jwt jwt = (Jwt) authentication.getPrincipal();
        if(jwt == null)
            throw new UserServiceException(ErrorCode.TOKEN_INVALID);

        String accessJwtId = jwt.getId();
        Instant accessExpiration = jwt.getExpiresAt();

        long ttl = ChronoUnit.SECONDS.between(
                Instant.now(),
                accessExpiration
        );

        if (ttl > 0) {
            redisTokenService.saveToken(
                    RedisToken.builder()
                            .jwtId(accessJwtId)
                            .userId(userId)
                            .expiration(ttl)
                            .build()
            );
        }
    }

    @Override
    public IntrospectResponse introspectToken(IntrospectRequest request) {
        try {
            SignedJWT signedJWT = jwtService.validateToken(request.token());
            String userId = signedJWT.getJWTClaimsSet().getSubject();
            Set<String> roles = extractRoles(signedJWT.getJWTClaimsSet().getClaim("roles"));
            return IntrospectResponse.builder()
                    .active(true)
                    .userId(userId)
                    .roles(roles)
                    .build();

        } catch (ParseException | JOSEException e) {
            return IntrospectResponse.builder()
                    .active(false)
                    .build();
        }
    }

    private Set<String> extractRoles(Object rolesClaim) {
        if (rolesClaim == null) {
            return Collections.emptySet();
        }

        if (rolesClaim instanceof Collection<?> collection) {
            return collection.stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
        }

        return Collections.emptySet();
    }


}
