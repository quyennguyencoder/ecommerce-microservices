package com.nguyenquyen.userservice.service.impl;

import com.nguyenquyen.userservice.client.MediaClient;
import com.nguyenquyen.userservice.common.RoleType;
import com.nguyenquyen.userservice.dto.request.CreateUserRequest;
import com.nguyenquyen.userservice.dto.response.CreateUserResponse;
import com.nguyenquyen.userservice.dto.response.FileResponse;
import com.nguyenquyen.userservice.dto.response.UserDetailResponse;
import com.nguyenquyen.userservice.entity.Role;
import com.nguyenquyen.userservice.entity.User;
import com.nguyenquyen.userservice.exception.ErrorCode;
import com.nguyenquyen.userservice.exception.UserServiceException;
import com.nguyenquyen.userservice.mapper.UserMapper;
import com.nguyenquyen.userservice.repository.UserRepository;
import com.nguyenquyen.userservice.service.RoleService;
import com.nguyenquyen.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "USER-SERVICE")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RoleService roleService;
    private final MediaClient mediaClient;

    @Override
    public CreateUserResponse createUser(CreateUserRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));

        Role role = roleService.createRole(RoleType.CUSTOMER.name());
        user.addRole(role);
        try {
            userRepository.save(user);
        }catch (DataIntegrityViolationException exception) {
            log.error("User already exists");
            throw new UserServiceException(ErrorCode.USER_ALREADY_EXISTS);
        }
        return userMapper.toCreateUserResponse(user);
    }

    @Override
    public UserDetailResponse myInfo(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserServiceException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserDetailResponse(user);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserDetailResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserDetailResponse)
                .toList();
    }

    @Override
    public String updateAvatar(String userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserServiceException(ErrorCode.USER_NOT_FOUND));

        try {
            FileResponse response = mediaClient.uploadFile(file).data();
            String avatarUrl = response.url();
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);
            return avatarUrl;
        } catch (Exception e) {
            log.error("Failed to upload file", e);
            throw new UserServiceException(ErrorCode.MEDIA_UPLOAD_FAILED);
        }
    }

}
