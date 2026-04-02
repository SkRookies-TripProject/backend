package com.costrip.costrip_backend.entity.enums;

public enum ExpenseCategory {

    FOOD("식비"),
    TRANSPORT("교통"),
    LODGING("숙박"),
    SHOPPING("쇼핑"),
    SIGHTSEEING("관광"),
    OTHER("기타");

    private final String label;

    ExpenseCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
