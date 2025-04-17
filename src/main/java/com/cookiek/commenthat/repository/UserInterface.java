package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInterface extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
    Optional<User> findByLoginId(String loginId);
    Optional<User> findByChannelName(String channelName);
}