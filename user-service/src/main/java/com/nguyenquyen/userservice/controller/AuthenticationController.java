package com.nguyenquyen.userservice.controller;

import com.nguyenquyen.userservice.dto.request.IntrospectRequest;
import com.nguyenquyen.userservice.dto.request.LoginRequest;
import com.nguyenquyen.userservice.dto.response.ApiResponse;
import com.nguyenquyen.userservice.dto.response.IntrospectResponse;
import com.nguyenquyen.userservice.dto.response.LoginResponse;
import com.nguyenquyen.userservice.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping( "/login")
    ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) {
        var data = authenticationService.login(request);

        Cookie cookie = new Cookie("refresh_token", data.accessToken());
        cookie.setHttpOnly(true); // Prevents JavaScript from accessing the cookie (XSS protection)
        cookie.setSecure(false); // Change to true in production
        cookie.setPath("/"); // Cookie is accessible across all paths in the app
        cookie.setMaxAge(14 * 24 * 60 * 60); // Cookie expiry: 14 days — matches refresh token TTL
        response.addCookie(cookie);

        return ApiResponse.<LoginResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Login successful")
                .data(data)
                .build();
    }

    @PostMapping("/refresh-token")
    ApiResponse<LoginResponse> refreshToken(@CookieValue("refresh_token") String refreshToken) {
        var data = authenticationService.refreshToken(refreshToken);

        return ApiResponse.<LoginResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Token refreshed successfully")
                .data(data)
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@CookieValue("refresh_token") String refreshToken, HttpServletResponse response) throws ParseException, JOSEException {
        authenticationService.logout(refreshToken);

        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Logout successful")
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody @Valid IntrospectRequest request) {
        var data = authenticationService.introspectToken(request);
        return ApiResponse.<IntrospectResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Token introspected successfully")
                .data(data)
                .build();
    }

}
