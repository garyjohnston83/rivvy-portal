package com.rivvystudios.portal.repository;

import com.rivvystudios.portal.model.Organization;
import com.rivvystudios.portal.model.OrganizationMember;
import com.rivvystudios.portal.model.ProducerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProducerAssignmentRepository extends JpaRepository<ProducerAssignment, UUID> {

    boolean existsByProducerMemberAndClientOrg(OrganizationMember producerMember, Organization clientOrg);
}
