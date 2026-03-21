package com.rivvystudios.portal.repository;

import com.rivvystudios.portal.model.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, UUID> {
}
