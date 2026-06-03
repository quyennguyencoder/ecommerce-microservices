package com.nguyenquyen.userservice.service;

import com.nguyenquyen.userservice.entity.RedisToken;

public interface RedisTokenService {

    void saveToken(RedisToken token);

    void deleteTokenByJwtId(String jwtId);

    boolean existsByJwtId(String jwtId);
}
