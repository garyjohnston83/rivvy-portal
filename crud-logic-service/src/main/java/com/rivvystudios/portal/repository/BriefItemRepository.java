package com.rivvystudios.portal.repository;

import com.rivvystudios.portal.model.BriefItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BriefItemRepository extends JpaRepository<BriefItem, UUID> {
}
