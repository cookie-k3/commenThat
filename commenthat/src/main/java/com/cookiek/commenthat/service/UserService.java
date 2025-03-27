package com.cookiek.commenthat.service;

import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public Long join(User user) {
        validateDuplicateUser(user);
        userRepository.save(user);
        return user.getId();
    }

    private void validateDuplicateUser(User user) {
        String userName = user.getName();
        List<User> users = userRepository.findByName(userName);
        if (!users.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 이름입니다.");
        }
    }

}
