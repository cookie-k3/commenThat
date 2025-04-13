package com.cookiek.commenthat.service;

import com.cookiek.commenthat.autoProcessor.service.NotSyncService;
import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    NotSyncService notSyncService;


    @Test
    public void 회원가입() throws Exception {
        //given
        User user = new User();
        user.setName("kmh");
        user.setEmail("db@naver.com");
        user.setPassword("dabin1234");
        user.setChannelName("연예 뒤통령이진호");
        user.setGender("여");
        user.setLoginId("dabin11");
        user.setNationality("kr");

        //when
        String channelName = user.getChannelName();
        String channelId = notSyncService.getChannelId(channelName);

        if (channelId == null) {
            throw new Exception("channelId null 오류");
        }

        user.setChannelId(channelId);
        Long savedId = userService.join(user);

        //then
        em.flush();
        assertEquals(user, userRepository.findById(savedId));

    }


}
