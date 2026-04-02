package com.daker.domain.xp.repository;

import com.daker.domain.xp.domain.UserXpHistory;
import com.daker.domain.xp.domain.XpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserXpHistoryRepository extends JpaRepository<UserXpHistory, Long> {

    boolean existsByUserIdAndHackathonIdAndType(Long userId, Long hackathonId, XpType type);

    @Query("SELECT x.user.id, SUM(x.amount) FROM UserXpHistory x GROUP BY x.user.id")
    List<Object[]> sumXpGroupByUser();
}
