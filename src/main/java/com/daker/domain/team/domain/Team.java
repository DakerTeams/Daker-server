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
    @JoinColumn(name = "hackathon_id", nullable = false)
    private Hackathon hackathon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean isOpen;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMember> members = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Team(Hackathon hackathon, User leader, String name, String description, boolean isOpen) {
        this.hackathon = hackathon;
        this.leader = leader;
        this.name = name;
        this.description = description;
        this.isOpen = isOpen;
    }

    public boolean isLeader(Long userId) {
        return this.leader.getId().equals(userId);
    }

    public boolean isFull(int maxTeamSize) {
        return this.members.size() >= maxTeamSize;
    }

    public void update(String name, String description, boolean isOpen) {
        this.name = name;
        this.description = description;
        this.isOpen = isOpen;
    }
}
