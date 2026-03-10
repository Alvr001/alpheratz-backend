package com.leonidas.alert.dto;

import com.leonidas.alert.Alert.AlertLevel;
import com.leonidas.alert.Alert.AlertType;

public record CreateAlertDto(
    AlertLevel level,
    AlertType type,
    String description,
    Double latitude,
    Double longitude,
    Long reporterId,
    Long groupId
) {}