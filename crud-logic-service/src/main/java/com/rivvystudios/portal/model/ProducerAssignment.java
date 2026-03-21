package com.rivvystudios.portal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "producer_assignment")
public class ProducerAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producer_member_id", nullable = false)
    private OrganizationMember producerMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_org_id", nullable = false)
    private Organization clientOrg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private UserAccount assignedBy;

    @Column(name = "assigned_at", nullable = false, columnDefinition = "timestamptz")
    private Instant assignedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OrganizationMember getProducerMember() {
        return producerMember;
    }

    public void setProducerMember(OrganizationMember producerMember) {
        this.producerMember = producerMember;
    }

    public Organization getClientOrg() {
        return clientOrg;
    }

    public void setClientOrg(Organization clientOrg) {
        this.clientOrg = clientOrg;
    }

    public UserAccount getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(UserAccount assignedBy) {
        this.assignedBy = assignedBy;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }
}
