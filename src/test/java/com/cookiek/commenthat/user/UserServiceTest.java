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

@SpringBootTest
@Transactional
@Rollback(value = false)
//@Rollback
public class UserServiceTest {

    @Autowired
    UserService userService;
    @Autowired
    UserInterface userInterface;
    @Autowired
    EntityManager em;
    @Autowired
    private UserRepository userRepository;


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

        String plainPassword = "dabin1234";
        String hashedPassword = SHA256Util.hash(plainPassword);

        System.out.println(" 해싱할 평문: " + plainPassword + " / 길이: " + plainPassword.length());
        System.out.println(" 해시 결과: " + hashedPassword);

        user.setPassword(hashedPassword);
        userInterface.save(user);
        em.flush();
        em.clear(); // ⚠ 꼭 clear 해서 강제로 DB에서 다시 가져오도록!

        userInterface.save(user);

        //  DB에서 다시 불러온 후 비교 확인
        User savedUser = userInterface.findByLoginId("dabin11").get();
        System.out.println("🗄 DB 저장된 password: " + savedUser.getPassword());
        System.out.println("🔁 입력 해시와 같은가? " + savedUser.getPassword().equals(SHA256Util.hash("dabin1234")));

    }


}