package com.amazon.automation.model;

import lombok.Builder;

@Builder
public record BookSnapshot(
        String title,
        String badge,
        String selectedType,
        String unitPrice,
        String totalPrice,
        String quantity) {

    public boolean hasBadge() {
        return badge != null && !badge.isBlank();
    }
}
