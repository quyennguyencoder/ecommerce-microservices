package com.nguyenquyen.userservice.mapper;

import com.nguyenquyen.userservice.dto.request.CreateUserRequest;
import com.nguyenquyen.userservice.dto.response.CreateUserResponse;
import com.nguyenquyen.userservice.dto.response.UserDetailResponse;
import com.nguyenquyen.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    User toUser(CreateUserRequest request);

    CreateUserResponse toCreateUserResponse(User user);

    UserDetailResponse toUserDetailResponse(User user);
}
