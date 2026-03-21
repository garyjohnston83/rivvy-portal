package com.rivvystudios.portal.repository;

import com.rivvystudios.portal.model.OrgRoleAssignment;
import com.rivvystudios.portal.model.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrgRoleAssignmentRepository extends JpaRepository<OrgRoleAssignment, UUID> {

    List<OrgRoleAssignment> findByOrganizationMemberIn(List<OrganizationMember> members);
}
