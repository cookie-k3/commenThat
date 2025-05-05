package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.Contents;
import com.cookiek.commenthat.domain.Reference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferenceInterface extends JpaRepository<Reference, Long> {
}
