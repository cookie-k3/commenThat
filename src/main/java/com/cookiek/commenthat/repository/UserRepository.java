package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final EntityManager em;

    public void save(User user) {
        if (user.getUserId() == null) {
            em.persist(user);
        }
        else {
            em.merge(user);
        }
    }

    public List<User> findByName(String name) {
        List<User> users = em.createQuery("select u from User u where u.name = :name", User.class)
                .setParameter("name", name)
                .getResultList();
        return users;
    }

    public User findById(Long user_id) {
        return em.find(User.class, user_id);
    }

    public User findByIdWithChannelInfos(Long userId) {
        return em.createQuery("SELECT u FROM User u LEFT JOIN FETCH u.channelInfos WHERE u.id = :userId", User.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    public Optional<User> findByIdOptional(Long userId) {
        try {
            User user = em.createQuery(
                            "SELECT u FROM User u " +
                                    "LEFT JOIN FETCH u.contents " +
                                    "LEFT JOIN FETCH u.channelInfos " +
                                    "LEFT JOIN FETCH u.videos " + // 모든 지연 로딩된 컬렉션을 포함
                                    "WHERE u.id = :userId", User.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByIdWithChannelInfosOptional(Long userId) {
        try {
            User user = em.createQuery("SELECT u FROM User u LEFT JOIN FETCH u.channelInfos WHERE u.id = :userId", User.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

}