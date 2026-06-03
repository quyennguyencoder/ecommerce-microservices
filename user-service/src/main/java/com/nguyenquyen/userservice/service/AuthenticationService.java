package com.nguyenquyen.userservice.service;

import com.nguyenquyen.userservice.dto.request.IntrospectRequest;
import com.nguyenquyen.userservice.dto.request.LoginRequest;
import com.nguyenquyen.userservice.dto.response.IntrospectResponse;
import com.nguyenquyen.userservice.dto.response.LoginResponse;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(String refreshToken);
    void logout(String refreshToken) throws ParseException, JOSEException;
    IntrospectResponse introspectToken(IntrospectRequest request);
}
