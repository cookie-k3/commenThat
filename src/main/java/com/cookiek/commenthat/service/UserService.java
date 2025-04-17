package com.cookiek.commenthat.service;

import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.dto.LoginRequestDto;
import com.cookiek.commenthat.dto.UserDto;
import com.cookiek.commenthat.repository.UserInterface;
import com.cookiek.commenthat.util.AES256Util;
import com.cookiek.commenthat.util.SHA256Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserInterface userInterface;

    public String register(UserDto userDto) {
        try {
            if (userInterface.findByEmail(userDto.getEmail()).isPresent()) {
                return "이미 사용 중인 이메일입니다.";
            }
            if (userInterface.findByLoginId(userDto.getLoginId()).isPresent()) {
                return "이미 사용 중인 아이디입니다.";
            }
            if (userInterface.findByChannelName(userDto.getChannelName()).isPresent()) {
                return "이미 사용 중인 채널명입니다.";
            }

            // 암호화 및 해싱
            String encryptedName = AES256Util.encrypt(userDto.getName());
            String encryptedEmail = AES256Util.encrypt(userDto.getEmail());
            String hashedPassword = SHA256Util.hash(userDto.getPassword());

            // DTO -> Entity 변환
            User user = User.builder()
                    .loginId(userDto.getLoginId())
                    .name(encryptedName)
                    .channelName(userDto.getChannelName())
                    .email(encryptedEmail)
                    .password(hashedPassword)
                    .nationality(userDto.getNationality())
                    .gender(userDto.getGender())
                    .build();

            userInterface.save(user);
            return "회원가입 완료!";
        } catch (Exception e) {
            logger.error("회원가입 중 오류 발생", e);
            return "회원가입 중 오류 발생: " + e.getMessage();
        }
    }

    public boolean login(LoginRequestDto loginDto) {
        Optional<User> optionalUser = userInterface.findByLoginId(loginDto.getLoginId());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            try {
                String hashedInputPassword = SHA256Util.hash(loginDto.getPassword());
                return user.getPassword().equals(hashedInputPassword);
            } catch (Exception e) {
                logger.error("비밀번호 해싱 중 오류 발생", e);
                return false;
            }
        }

        return false;
    }

    // 사용자 정보를 반환하는 로그인 메서드
    public UserDto loginWithUserInfo(LoginRequestDto loginDto) {
        Optional<User> optionalUser = userInterface.findByLoginId(loginDto.getLoginId());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            try {
                // 로그 출력(확인용)
                System.out.println("클라이언트로부터 받은 로그인 ID: " + loginDto.getLoginId());
                System.out.println("클라이언트로부터 받은 PW: " + loginDto.getPassword());
                System.out.println("해싱된 입력 PW: " + SHA256Util.hash(loginDto.getPassword()));
                System.out.println("DB 저장된 PW: " + user.getPassword());
                System.out.println("일치 여부: " + user.getPassword().equals(SHA256Util.hash(loginDto.getPassword())));

                String hashedInputPassword = SHA256Util.hash(loginDto.getPassword());
                boolean isMatch = user.getPassword().equals(hashedInputPassword);

                if (isMatch) {
                    // 복호화 예외 방지
                    String decryptedName = "복호화 실패";
                    String decryptedEmail = "복호화 실패";
                    try {
                        decryptedName = AES256Util.decrypt(user.getName());
                        decryptedEmail = AES256Util.decrypt(user.getEmail());
                    } catch (Exception e) {
                        logger.error("복호화 중 오류 발생!", e);
                    }

                    return UserDto.builder()
                            .userId(user.getUserId())
                            .loginId(user.getLoginId())
                            .name(decryptedName)
                            .channelName(user.getChannelName())
                            .email(decryptedEmail)
                            .password(null)
                            .nationality(user.getNationality())
                            .gender(user.getGender())
                            .build();
                }

            } catch (Exception e) {
                logger.error("비밀번호 비교 중 오류", e);
            }
        }

        return null;
    }
}