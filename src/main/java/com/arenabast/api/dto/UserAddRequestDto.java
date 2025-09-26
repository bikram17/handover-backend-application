package com.arenabast.api.dto;

import com.arenabast.api.enums.RoleTypes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAddRequestDto {
    String name;
    @NotBlank(message = "Username is mandatory")
    @Size(min = 8, max = 20, message = "Username must be between 8 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Username can only contain letters and numbers, no spaces or special characters")
    String userName;
    String email;
    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    String password;
    RoleTypes role;
    Long adminId;
}
