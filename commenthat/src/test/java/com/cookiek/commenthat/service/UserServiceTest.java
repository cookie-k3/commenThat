package com.cookiek.commenthat.service;

import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Transactional
@Rollback(value = false)
//@Rollback
public class UserServiceTest {

    @Autowired UserService userService;
    @Autowired UserRepository userRepository;
    @Autowired EntityManager em;

    @Value("${youtube.api.key}")
    private String api;

    @Test
    public void 회원가입() throws Exception {
        //given
        User user = new User();
        user.setName("dabin");
        user.setEmail("db@naver.com");
        user.setPassword("dabin1234");
        user.setChannelName("워크맨-Workman");

        //when
        Long savedId = userService.join(user);

        //then
        em.flush();
        assertEquals(user, userRepository.findById(savedId));

    }


}
