package com.rivvystudios.portal.persistence;

import com.rivvystudios.portal.TestcontainersConfiguration;
import com.rivvystudios.portal.model.Role;
import com.rivvystudios.portal.repository.ApprovalEventRepository;
import com.rivvystudios.portal.repository.ApprovalRequestRepository;
import com.rivvystudios.portal.repository.BrandAssetRepository;
import com.rivvystudios.portal.repository.BrandAssetVersionRepository;
import com.rivvystudios.portal.repository.BriefItemDeliverableRepository;
import com.rivvystudios.portal.repository.BriefItemRepository;
import com.rivvystudios.portal.repository.BriefRepository;
import com.rivvystudios.portal.repository.CommentMentionRepository;
import com.rivvystudios.portal.repository.OrgRoleAssignmentRepository;
import com.rivvystudios.portal.repository.OrganizationMemberRepository;
import com.rivvystudios.portal.repository.OrganizationRepository;
import com.rivvystudios.portal.repository.ProducerAssignmentRepository;
import com.rivvystudios.portal.repository.ProjectRepository;
import com.rivvystudios.portal.repository.ReviewCommentRepository;
import com.rivvystudios.portal.repository.RoleRepository;
import com.rivvystudios.portal.repository.StorageObjectRepository;
import com.rivvystudios.portal.repository.UserAccountRepository;
import com.rivvystudios.portal.repository.VideoRepository;
import com.rivvystudios.portal.repository.VideoVersionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SchemaValidationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void contextLoads() {
        // If this test runs, Hibernate validate passed against the Liquibase schema
        assertThat(context).isNotNull();
    }

    @Test
    void allRepositoryBeansExist() {
        assertThat(context.getBean(RoleRepository.class)).isNotNull();
        assertThat(context.getBean(OrganizationRepository.class)).isNotNull();
        assertThat(context.getBean(StorageObjectRepository.class)).isNotNull();
        assertThat(context.getBean(UserAccountRepository.class)).isNotNull();
        assertThat(context.getBean(OrganizationMemberRepository.class)).isNotNull();
        assertThat(context.getBean(OrgRoleAssignmentRepository.class)).isNotNull();
        assertThat(context.getBean(BriefRepository.class)).isNotNull();
        assertThat(context.getBean(BriefItemRepository.class)).isNotNull();
        assertThat(context.getBean(BriefItemDeliverableRepository.class)).isNotNull();
        assertThat(context.getBean(ProjectRepository.class)).isNotNull();
        assertThat(context.getBean(BrandAssetRepository.class)).isNotNull();
        assertThat(context.getBean(BrandAssetVersionRepository.class)).isNotNull();
        assertThat(context.getBean(VideoRepository.class)).isNotNull();
        assertThat(context.getBean(VideoVersionRepository.class)).isNotNull();
        assertThat(context.getBean(ReviewCommentRepository.class)).isNotNull();
        assertThat(context.getBean(CommentMentionRepository.class)).isNotNull();
        assertThat(context.getBean(ProducerAssignmentRepository.class)).isNotNull();
        assertThat(context.getBean(ApprovalRequestRepository.class)).isNotNull();
        assertThat(context.getBean(ApprovalEventRepository.class)).isNotNull();
    }

    @Test
    void seedDataLoaded() {
        List<Role> roles = roleRepository.findAll();
        assertThat(roles).hasSize(3);
        assertThat(roles).extracting(Role::getCode)
                .containsExactlyInAnyOrder("RIVVY_ADMIN", "RIVVY_PRODUCER", "CLIENT");
    }
}
