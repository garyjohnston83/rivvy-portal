package com.rivvystudios.portal.repository;

import com.rivvystudios.portal.model.StorageObject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StorageObjectRepository extends JpaRepository<StorageObject, UUID> {
}
