package com.leonidas.alert;

import java.util.List;

import org.springframework.stereotype.Service;

import com.leonidas.alert.dto.CreateAlertDto;
import com.leonidas.group.Group;
import com.leonidas.group.GroupRepository;
import com.leonidas.user.User;
import com.leonidas.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public Alert createAlert(CreateAlertDto dto) {
        User reporter = userRepository.findById(dto.reporterId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Group group = groupRepository.findById(dto.groupId())
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        Alert alert = new Alert();
        alert.setLevel(dto.level());
        alert.setType(dto.type());
        alert.setDescription(dto.description());
        alert.setLatitude(dto.latitude());
        alert.setLongitude(dto.longitude());
        alert.setReporter(reporter);
        alert.setGroup(group);

        return alertRepository.save(alert);
    }

    public List<Alert> getAlertsByGroup(Long groupId) {
        return alertRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
    }

    public List<Alert> getActiveAlerts(Long groupId) {
        return alertRepository.findByGroupIdAndStatus(groupId, Alert.AlertStatus.ACTIVE);
    }

    public Alert resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));
        alert.setStatus(Alert.AlertStatus.RESOLVED);
        return alertRepository.save(alert);
    }

    public Alert markAsFalseAlarm(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));
        alert.setStatus(Alert.AlertStatus.FALSE_ALARM);
        return alertRepository.save(alert);
    }
}