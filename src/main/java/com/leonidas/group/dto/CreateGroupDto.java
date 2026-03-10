package com.leonidas.group.dto;

public record CreateGroupDto(
    String name,
    String description,
    Long adminId
) {}