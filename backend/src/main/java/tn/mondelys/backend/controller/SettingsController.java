package tn.mondelys.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tn.mondelys.backend.dto.SettingsDtos;
import tn.mondelys.backend.service.SettingsService;

import java.util.Map;

@RestController
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping("/api/settings/public")
    public ResponseEntity<SettingsDtos.SettingsResponse> publicSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @GetMapping("/api/admin/settings")
    public ResponseEntity<SettingsDtos.SettingsResponse> adminSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @PutMapping("/api/admin/settings")
    public ResponseEntity<SettingsDtos.SettingsResponse> updateSettings(
            @Valid @RequestBody SettingsDtos.UpdateSettingsRequest request
    ) {
        return ResponseEntity.ok(settingsService.updateSettings(request));
    }

    @PostMapping("/api/admin/settings/reset-operational-data")
    public ResponseEntity<Map<String, String>> resetOperationalData() {
        settingsService.resetOperationalData();
        return ResponseEntity.ok(Map.of("message", "Les réservations, messages et avis ont été supprimés."));
    }
}
