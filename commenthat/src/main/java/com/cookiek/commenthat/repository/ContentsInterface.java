package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.Contents;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentsInterface extends JpaRepository<Contents, Long> {
}
