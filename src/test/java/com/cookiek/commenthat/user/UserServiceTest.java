package com.cookiek.commenthat.user;

import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.repository.UserInterface;
import com.cookiek.commenthat.repository.UserRepository;
import com.cookiek.commenthat.service.UserService;
import com.cookiek.commenthat.util.SHA256Util;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@Rollback(value = false)
//@Rollback
public class UserServiceTest {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EntityManager em;


    @Test
    public void 회원가입() throws Exception {
        //given
        User user = new User();
        user.setName("kmh");
        user.setEmail("db@naver.com");
        user.setPassword("dabin1234");
        user.setChannelName("연예 뒤통령이진호");
//        user.setGender("여");
        user.setLoginId("dabin11");
        user.setNationality("kr");

        //when
        String channelName = user.getChannelName();
//        String channelId = notSyncService.getChannelId(channelName);

//        if (channelId == null) {
//            throw new Exception("channelId null 오류");
//        }
//
//        user.setChannelId(channelId);
//        Long savedId = userService.join(user);

        //then
        em.flush();
//        assertEquals(user, userRepository.findById(savedId));

    }

    @Test
    public void 회원수정() throws Exception {
        Long userId = 2L;

        User user = userRepository.findById(userId);

        String hashedPassword = SHA256Util.hash(user.getPassword());

        user.setPassword(hashedPassword);

        userRepository.save(user);

        em.flush();

    }


}