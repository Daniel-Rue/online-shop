package ru.kon.onlineshop.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String middleName;

    @Email
    @NotBlank
    private String email;

    @Size(min = 8)
    @NotBlank
    private String password;
}
