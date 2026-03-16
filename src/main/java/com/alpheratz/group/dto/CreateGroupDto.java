package com.alpheratz.group.dto;

public record CreateGroupDto(
    String name,
    String description,
    Long adminId
) {}