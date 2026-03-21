package com.rivvystudios.portal.repository;

import com.rivvystudios.portal.model.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, UUID> {
}
