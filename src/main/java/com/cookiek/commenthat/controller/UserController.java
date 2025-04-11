package com.cookiek.commenthat.controller;

import com.cookiek.commenthat.common.ApiResponse;
import com.cookiek.commenthat.dto.UserDto;
import com.cookiek.commenthat.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cookiek.commenthat.dto.LoginRequestDto;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody UserDto userDto) {
        String result = userService.register(userDto);

        if (result.equals("회원가입 완료!")) {
            return ResponseEntity.ok(ApiResponse.success(result, null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.fail(result));
        }
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequestDto loginDto) {
        boolean isSuccess = userService.login(loginDto);

        if (isSuccess) {
            return ResponseEntity.ok(ApiResponse.success("로그인 성공!", null));
        } else {
            return ResponseEntity.status(401).body(ApiResponse.fail("아이디 또는 비밀번호가 올바르지 않습니다."));
        }
    }
}