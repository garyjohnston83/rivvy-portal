package com.rivvystudios.portal.repository;

import com.rivvystudios.portal.model.CommentMention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentMentionRepository extends JpaRepository<CommentMention, UUID> {
}
