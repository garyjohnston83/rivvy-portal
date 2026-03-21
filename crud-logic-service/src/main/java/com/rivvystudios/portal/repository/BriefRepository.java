package com.rivvystudios.portal.repository;

import com.rivvystudios.portal.model.Brief;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BriefRepository extends JpaRepository<Brief, UUID> {
}
