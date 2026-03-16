package com.alpheratz.alert.dto;

import com.alpheratz.alert.Alert.AlertLevel;
import com.alpheratz.alert.Alert.AlertType;

public record CreateAlertDto(
    AlertLevel level,
    AlertType type,
    String description,
    Double latitude,
    Double longitude,
    Long reporterId,
    Long groupId
) {}