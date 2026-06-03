package com.nguyenquyen.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nguyenquyen.userservice.common.Gender;
import com.nguyenquyen.userservice.common.UserStatus;
import lombok.Builder;
import java.time.LocalDate;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Builder
@JsonInclude(NON_NULL)
public record UserDetailResponse(
        String email,
        String firstName,
        String lastName,
        String phone,
        String avatarKey,
        Gender gender,
        LocalDate birthDate,
        UserStatus userStatus
) {
}
