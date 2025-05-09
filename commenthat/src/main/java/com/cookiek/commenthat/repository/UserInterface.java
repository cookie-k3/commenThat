package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.domain.Video;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserInterface extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"contents", "channelInfos", "videos"})
    Optional<User> findById(Long id);

//    List<User> findAllByIdGreaterThan(Long userId);

}

