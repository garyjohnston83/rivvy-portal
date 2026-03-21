package com.rivvystudios.portal.repository;

import com.rivvystudios.portal.model.ApprovalEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApprovalEventRepository extends JpaRepository<ApprovalEvent, UUID> {
}
