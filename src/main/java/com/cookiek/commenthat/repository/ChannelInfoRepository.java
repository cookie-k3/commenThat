package com.cookiek.commenthat.repository;


import com.cookiek.commenthat.domain.ChannelInfo;
import com.cookiek.commenthat.domain.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChannelInfoRepository {

    private final EntityManager em;
    private final UserRepository userRepository;

    public void save(ChannelInfo channelInfo) {
        if (channelInfo.getId() == null) {
            em.persist(channelInfo);
        }
        else {
            em.merge(channelInfo);
        }
    }

}

