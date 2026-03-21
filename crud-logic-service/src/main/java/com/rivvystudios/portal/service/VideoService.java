package com.rivvystudios.portal.service;

import com.rivvystudios.portal.controller.dto.VideoDetailResponse;
import com.rivvystudios.portal.controller.dto.VideoListItemResponse;
import com.rivvystudios.portal.model.OrganizationMember;
import com.rivvystudios.portal.model.Project;
import com.rivvystudios.portal.model.UserAccount;
import com.rivvystudios.portal.model.Video;
import com.rivvystudios.portal.model.VideoVersion;
import com.rivvystudios.portal.model.enums.TranscodeStatus;
import com.rivvystudios.portal.repository.OrganizationMemberRepository;
import com.rivvystudios.portal.repository.ProducerAssignmentRepository;
import com.rivvystudios.portal.repository.ProjectRepository;
import com.rivvystudios.portal.repository.UserAccountRepository;
import com.rivvystudios.portal.repository.VideoRepository;
import com.rivvystudios.portal.repository.VideoVersionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final VideoVersionRepository videoVersionRepository;
    private final UserAccountRepository userAccountRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final ProducerAssignmentRepository producerAssignmentRepository;
    private final ProjectRepository projectRepository;

    public VideoService(VideoRepository videoRepository,
                        VideoVersionRepository videoVersionRepository,
                        UserAccountRepository userAccountRepository,
                        OrganizationMemberRepository organizationMemberRepository,
                        ProducerAssignmentRepository producerAssignmentRepository,
                        ProjectRepository projectRepository) {
        this.videoRepository = videoRepository;
        this.videoVersionRepository = videoVersionRepository;
        this.userAccountRepository = userAccountRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.producerAssignmentRepository = producerAssignmentRepository;
        this.projectRepository = projectRepository;
    }

    public Page<VideoListItemResponse> getVideosByProject(String email, UUID projectId, int page) {
        authorizeForProject(email, projectId);

        Page<Video> videoPage = videoRepository.findByProjectId(projectId,
                PageRequest.of(page, 25, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("title"))));

        List<UUID> videoIds = videoPage.getContent().stream()
                .map(Video::getId)
                .collect(Collectors.toList());

        List<VideoVersion> allVersions = videoIds.isEmpty()
                ? List.of()
                : videoVersionRepository.findByVideoIdIn(videoIds);

        Map<UUID, List<VideoVersion>> versionsByVideoId = allVersions.stream()
                .collect(Collectors.groupingBy(vv -> vv.getVideo().getId()));

        return videoPage.map(video -> {
            List<VideoVersion> versions = versionsByVideoId.getOrDefault(video.getId(), List.of());

            Integer currentVersionNumber = versions.stream()
                    .filter(vv -> Boolean.TRUE.equals(vv.getIsCurrent()))
                    .findFirst()
                    .map(VideoVersion::getVersionNumber)
                    .orElseGet(() -> versions.stream()
                            .max(Comparator.comparingInt(VideoVersion::getVersionNumber))
                            .map(VideoVersion::getVersionNumber)
                            .orElse(null));

            boolean isApproved = versions.stream()
                    .anyMatch(vv -> Boolean.TRUE.equals(vv.getIsApproved()));

            return new VideoListItemResponse(video.getId(), video.getTitle(), currentVersionNumber, isApproved);
        });
    }

    public VideoDetailResponse getVideoDetail(String email, UUID videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Video not found"));

        authorizeForProject(email, video.getProject().getId());

        Optional<VideoVersion> latestVersion = videoVersionRepository.findByVideoIdAndIsCurrentTrue(videoId);
        if (latestVersion.isEmpty()) {
            latestVersion = videoVersionRepository.findFirstByVideoIdOrderByVersionNumberDesc(videoId);
        }

        Integer currentVersionNumber = latestVersion.map(VideoVersion::getVersionNumber).orElse(null);
        boolean isApproved = videoVersionRepository.existsByVideoIdAndIsApprovedTrue(videoId);

        String transcodeStatus = latestVersion.map(vv -> vv.getTranscodeStatus().name()).orElse(null);

        String playbackUrl = null;
        if (latestVersion.isPresent() && latestVersion.get().getTranscodeStatus() == TranscodeStatus.COMPLETED) {
            UUID storageObjectId = latestVersion.get().getStorageObject().getId();
            playbackUrl = "https://storage.example.com/stub-presigned/" + storageObjectId + "?token=stub";
        }

        String createdAt = video.getCreatedAt() != null ? video.getCreatedAt().toString() : null;

        return new VideoDetailResponse(
                video.getId(),
                video.getTitle(),
                video.getDescription(),
                currentVersionNumber,
                isApproved,
                transcodeStatus,
                playbackUrl,
                createdAt
        );
    }

    private void authorizeForProject(String email, UUID projectId) {
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        List<OrganizationMember> memberships = organizationMemberRepository.findByUserAccount(userAccount);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Project not found"));

        // Check if user is a member of the project's organization
        boolean isOrgMember = memberships.stream()
                .anyMatch(m -> m.getOrganization().getId().equals(project.getOrganization().getId()));

        if (isOrgMember) {
            return;
        }

        // Check if user is a producer assigned to the project's organization
        boolean isAssignedProducer = memberships.stream()
                .anyMatch(m -> producerAssignmentRepository.existsByProducerMemberAndClientOrg(m, project.getOrganization()));

        if (isAssignedProducer) {
            return;
        }

        throw new ResponseStatusException(FORBIDDEN, "Access denied");
    }
}
