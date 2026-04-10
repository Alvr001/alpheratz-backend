package com.alpheratz.alert.dto;

import com.alpheratz.alert.Alert;

public record CreateAlertDto(
    Long groupId,
    Long reporterId,
    Alert.AlertLevel level,
    Alert.AlertType type,
    String description
) {}