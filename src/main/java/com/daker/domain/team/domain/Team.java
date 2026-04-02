package com.daker.domain.team.domain;

import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hackathon_id")
    private Hackathon hackathon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User leader;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamStatus status = TeamStatus.OPEN;

    @Column(nullable = false)
    private boolean isOpen = true;

    @Column(nullable = false)
    private boolean isPublic = true;

    @Column(nullable = false)
    private int currentMemberCount = 1;

    @Column(nullable = false)
    private int maxMemberCount = 5;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamPosition> positions = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @Builder
    public Team(Hackathon hackathon, User leader, String name, String description,
                boolean isOpen, boolean isPublic, int maxMemberCount) {
        this.hackathon = hackathon;
        this.leader = leader;
        this.name = name;
        this.description = description;
        this.status = TeamStatus.OPEN;
        this.isOpen = isOpen;
        this.isPublic = isPublic;
        this.maxMemberCount = maxMemberCount > 0 ? maxMemberCount : 5;
        this.currentMemberCount = 1;
    }

    public boolean isLeader(Long userId) {
        return this.leader.getId().equals(userId);
    }

    public boolean isFull() {
        return this.currentMemberCount >= this.maxMemberCount;
    }

    public void incrementMemberCount() {
        this.currentMemberCount++;
    }

    public void decrementMemberCount() {
        if (this.currentMemberCount > 0) this.currentMemberCount--;
    }

    public void update(String name, String description, Boolean isOpen) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (isOpen != null) this.isOpen = isOpen;
    }

    public void updateMaxMemberCount(Integer maxMemberCount) {
        if (maxMemberCount != null && maxMemberCount > 0) {
            this.maxMemberCount = maxMemberCount;
        }
    }

    public void linkHackathon(Hackathon hackathon) {
        this.hackathon = hackathon;
    }

    public void close() {
        this.status = TeamStatus.CLOSED;
        this.isOpen = false;
    }

    public void softDelete() {
        this.status = TeamStatus.DELETED;
        this.deletedAt = java.time.LocalDateTime.now();
    }
}
