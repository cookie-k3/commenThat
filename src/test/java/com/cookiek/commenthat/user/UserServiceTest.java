package com.cookiek.commenthat.user;


import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.repository.UserInterface;
import com.cookiek.commenthat.repository.UserRepository;
import com.cookiek.commenthat.service.UserService;
import com.cookiek.commenthat.util.AES256Util;
import com.cookiek.commenthat.util.SHA256Util;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
//@Rollback(value = false)
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
        // given: 사용자 정보 생성
        User user = new User();
        user.setName("cookiek");
        user.setEmail("cookiek@gmail.com");
        user.setPassword("cookiek1234");
        user.setChannelName("cookiek channel");
        user.setLoginId("cookiek11");
        user.setNationality("kr");

        // 암호화/해싱 처리 (실제 서비스 흐름과 맞추기 위해 수동 적용)
        String encryptedName = AES256Util.encrypt(user.getName());
        String encryptedEmail = AES256Util.encrypt(user.getEmail());
        String hashedPassword = SHA256Util.hash(user.getPassword());

        user.setName(encryptedName);
        user.setEmail(encryptedEmail);
        user.setPassword(hashedPassword);

        // when: 저장
        userInterface.save(user);
        em.flush();
        em.clear(); // 실제 DB에서 다시 조회되도록

        // then: 저장된 사용자 확인 및 암호화/해싱 여부 확인
        User savedUser = userInterface.findByLoginId("cookiek11").get();

        System.out.println("저장된 이름 (암호화): " + savedUser.getName());
        System.out.println("복호화된 이름: " + AES256Util.decrypt(savedUser.getName()));

        System.out.println("저장된 이메일 (암호화): " + savedUser.getEmail());
        System.out.println("복호화된 이메일: " + AES256Util.decrypt(savedUser.getEmail()));

        System.out.println("저장된 비밀번호 (SHA-256 해시): " + savedUser.getPassword());
        System.out.println("입력값 해시와 일치? " +
                savedUser.getPassword().equals(SHA256Util.hash("cookiek1234")));
    }
//    @Test
//    public void 회원가입() throws Exception {
        //given
//        User user = new User();
//        user.setName("cookiek");
//        user.setEmail("cookiek@gmail.com");
//        user.setPassword("cookiek1234");
//        user.setChannelName("cookiek channel");
//        user.setGender("여");
//        user.setLoginId("cookiek11");
//        user.setNationality("kr");

        //when
//        String channelName = user.getChannelName();
//        String channelId = notSyncService.getChannelId(channelName);

//        if (channelId == null) {
//            throw new Exception("channelId null 오류");
//        }
//
//        user.setChannelId(channelId);
//        Long savedId = userService.join(user);

        //then
//        em.flush();
//        assertEquals(user, userRepository.findById(savedId));

//    }

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