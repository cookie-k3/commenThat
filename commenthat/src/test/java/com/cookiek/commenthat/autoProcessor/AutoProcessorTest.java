package com.cookiek.commenthat.autoProcessor;

import com.cookiek.commenthat.autoProcessor.service.FetchInitialDataService;
import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.repository.UserRepository;
import com.cookiek.commenthat.service.ChannelInfoService;
import com.cookiek.commenthat.service.UserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
//@AutoConfigureMockMvc
@Transactional
@Rollback(value = false)
//@Rollback
public class AutoProcessorTest {

    @Autowired
    UserService userService;
    @Autowired
    ChannelInfoService channelInfoService;
    @Autowired EntityManager em;

//    @Autowired
//    /**
//     * 웹 API 테스트할 때 사용
//     * 스프링 MVC 테스트의 시작점
//     * HTTP GET,POST 등에 대해 API 테스트 가능
//     * */
//    MockMvc mvc;

    @Value("${youtube.api.key}")
    private String api;

    @Test
    public void testFetchChannelInfo() throws Exception {
        //given
//        User user = new User();
//        user.setName("test");
//        user.setEmail("test");
//        user.setPassword("test");
//        user.setChannelName("워크맨-Workman");
//
//        Long userId = userService.join(user);

        Long userId = 1L;

        //when
        channelInfoService.fetchInitData(userId);

        // Then
        em.flush();








//        mvc.perform(get("/fetch-channel-info")
//                .param("channelName", channelName)
//                .param("userId", Long.toString(savedId))
//                )
//                .andExpect(status().isOk());

    }

}

//http://localhost:8080/fetch-channel-info?userId=1