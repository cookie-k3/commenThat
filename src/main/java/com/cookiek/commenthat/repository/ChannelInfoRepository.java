package com.cookiek.commenthat.repository;


import com.cookiek.commenthat.domain.ChannelInfo;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChannelInfoRepository {

    private final EntityManager em;
    private final UserInterface userInterface;

    public void save(ChannelInfo channelInfo) {
        if (channelInfo.getId() == null) {
            em.persist(channelInfo);
        }
        else {
            em.merge(channelInfo);
        }
    }

}

