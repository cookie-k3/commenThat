package com.cookiek.commenthat.service;

import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.dto.LoginRequestDto;
import com.cookiek.commenthat.dto.UserDto;
import com.cookiek.commenthat.repository.UserRepository;
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
    private final UserRepository userRepository;

    public String register(UserDto userDto) {
        try {
            if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
                return "이미 사용 중인 이메일입니다.";
            }
            if (userRepository.findByLoginId(userDto.getLoginId()).isPresent()) {
                return "이미 사용 중인 아이디입니다.";
            }
            if (userRepository.findByChannelName(userDto.getChannelName()).isPresent()) {
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

            userRepository.save(user);
            return "회원가입 완료!";
        } catch (Exception e) {
            logger.error("회원가입 중 오류 발생", e); // robust logging <-> printStackTrace(): 예외 발생 내용을 콘솔에만 출력

            return "회원가입 중 오류 발생: " + e.getMessage();
        }
    }

    public boolean login(LoginRequestDto loginDto) {
        Optional<User> optionalUser = userRepository.findByLoginId(loginDto.getLoginId());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            try {
                if (user.getUserId() == 2L) {
                    //  user_id 2번만 평문 비교
                    return user.getPassword().equals(loginDto.getPassword());
                }

                // 나머지 유저는 기존대로 해싱하여 비교
                String hashedInputPassword = SHA256Util.hash(loginDto.getPassword());
                return user.getPassword().equals(hashedInputPassword);

            } catch (Exception e) {
                logger.error("비밀번호 해싱 중 오류 발생", e);
                return false;
            }
        }

        return false;
    }
}