package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final EntityManager em;

    public void save(User user) {
        if (user.getId() == null) {
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

    public User findOne(Long user_id) {
        return em.find(User.class, user_id);
    }

}
