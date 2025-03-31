package ru.kon.onlineshop.dto.user;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private Instant createdAt;
}
