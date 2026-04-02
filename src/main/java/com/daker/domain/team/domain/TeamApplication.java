package com.daker.domain.team.domain;

import com.daker.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_applications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TeamApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(length = 100)
    private String position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_user_id")
    private User processedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private LocalDateTime deletedAt;

    @Builder
    public TeamApplication(Team team, User user, String message, String position) {
        this.team = team;
        this.user = user;
        this.message = message;
        this.position = position;
        this.status = ApplicationStatus.PENDING;
    }

    public void accept(User processedBy) {
        this.status = ApplicationStatus.ACCEPTED;
        this.processedBy = processedBy;
        this.processedAt = LocalDateTime.now();
    }

    public void reject(User processedBy) {
        this.status = ApplicationStatus.REJECTED;
        this.processedBy = processedBy;
        this.processedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = ApplicationStatus.CANCELED;
        this.deletedAt = LocalDateTime.now();
    }
}
