package com.rivvystudios.portal.repository;

import com.rivvystudios.portal.model.OrganizationMember;
import com.rivvystudios.portal.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {

    List<OrganizationMember> findByUserAccount(UserAccount userAccount);
}
