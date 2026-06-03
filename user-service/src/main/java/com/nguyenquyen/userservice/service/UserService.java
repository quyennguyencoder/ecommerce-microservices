package com.nguyenquyen.userservice.service;

import com.nguyenquyen.userservice.dto.request.CreateUserRequest;
import com.nguyenquyen.userservice.dto.response.CreateUserResponse;
import com.nguyenquyen.userservice.dto.response.UserDetailResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    CreateUserResponse createUser(CreateUserRequest request);
    UserDetailResponse myInfo(String userId);
    List<UserDetailResponse> getAllUsers();
    String updateAvatar(String userId, MultipartFile file);
}
