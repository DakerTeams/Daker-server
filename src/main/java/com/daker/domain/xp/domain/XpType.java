package com.daker.domain.xp.domain;

public enum XpType {
    AWARD_1ST(500),
    AWARD_2ND(300),
    AWARD_3RD(200);

    private final int amount;

    XpType(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
