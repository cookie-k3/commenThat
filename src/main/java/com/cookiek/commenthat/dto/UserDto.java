package com.cookiek.commenthat.dto;

import com.cookiek.commenthat.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long userId;

    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문 또는 영문+숫자만 가능합니다.")
    @NotBlank(message = "아이디는 필수 항목입니다.")
    private String loginId;

    @NotBlank(message = "이름은 필수 항목입니다.")
    private String name;

    @NotBlank(message = "채널명은 필수 항목입니다.")
    private String channelName;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수 항목입니다.")
    private String email;


    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    private String password;

    private String nationality;

    private Gender gender;
}