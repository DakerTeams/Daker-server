package com.daker.domain.ranking.dto;

import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;

import java.time.LocalDateTime;

public enum RankingPeriod {

    ALL("all"),
    THIRTY_DAYS("30d"),
    SEVEN_DAYS("7d");

    private final String value;

    RankingPeriod(String value) {
        this.value = value;
    }

    public static RankingPeriod from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }

        for (RankingPeriod period : values()) {
            if (period.value.equalsIgnoreCase(value)) {
                return period;
            }
        }

        throw new CustomException(ErrorCode.INVALID_INPUT);
    }

    public LocalDateTime resolveStartDateTime() {
        LocalDateTime now = LocalDateTime.now();

        return switch (this) {
            case ALL -> null;
            case THIRTY_DAYS -> now.minusDays(30);
            case SEVEN_DAYS -> now.minusDays(7);
        };
    }
}
