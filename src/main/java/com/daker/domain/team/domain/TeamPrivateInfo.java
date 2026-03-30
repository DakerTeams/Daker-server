package com.daker.domain.team.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_private_infos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TeamPrivateInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false, unique = true)
    private Team team;

    @Column(length = 30)
    private String contactType;

    @Column(length = 500)
    private String contactValue;

    @Column(columnDefinition = "TEXT")
    private String internalMemo;

    @Column(length = 255)
    private String editToken;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public TeamPrivateInfo(Team team, String contactType, String contactValue,
                           String internalMemo, String editToken) {
        this.team = team;
        this.contactType = contactType;
        this.contactValue = contactValue;
        this.internalMemo = internalMemo;
        this.editToken = editToken;
    }
}
