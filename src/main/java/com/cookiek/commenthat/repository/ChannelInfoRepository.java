package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.ChannelInfo;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChannelInfoRepository {

    private final EntityManager em;

    public void save(ChannelInfo channelInfo) {
        if (channelInfo.getId() == null) {
            em.persist(channelInfo);
        } else {
            em.merge(channelInfo);
        }
    }

    //userId로 ChannelInfo 리스트를 조회하고 날짜 오름차순 정렬
    public List<ChannelInfo> findByUserIdOrderByDateAsc(Long userId) {
        return em.createQuery(
                        "SELECT c FROM ChannelInfo c WHERE c.user.id = :userId ORDER BY c.date ASC",
                        ChannelInfo.class
                ).setParameter("userId", userId)
                .getResultList();
    }
}