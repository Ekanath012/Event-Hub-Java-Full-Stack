package com.eventhub.dto;

import com.eventhub.entity.Category;

public record CategoryResponse(Long id, String name, String description, boolean active) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), c.isActive());
    }
}
