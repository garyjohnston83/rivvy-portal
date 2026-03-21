package com.rivvystudios.portal.repository;

import com.rivvystudios.portal.model.BriefItemDeliverable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BriefItemDeliverableRepository extends JpaRepository<BriefItemDeliverable, UUID> {
}
