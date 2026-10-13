package com.deskwell.web.service;

import com.deskwell.web.model.Setting;
import com.deskwell.web.repository.SettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettingsService {

    private final SettingRepository settingRepository;

    public SettingsService(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public Map<String, String> getAllSettings() {
        Map<String, String> map = new HashMap<>();
        List<Setting> list = settingRepository.findAll();
        for (Setting s : list) {
            map.put(s.getKey(), s.getValue());
        }
        return map;
    }

    public String getSetting(String key, String defaultValue) {
        return settingRepository.findById(key)
            .map(Setting::getValue)
            .orElse(defaultValue);
    }

    @Transactional
    public void setSetting(String key, String value) {
        Setting s = settingRepository.findById(key).orElse(new Setting(key, value));
        s.setValue(value);
        settingRepository.save(s);
    }

    @Transactional
    public Map<String, String> updateBulkSettings(Map<String, String> newSettings) {
        for (Map.Entry<String, String> entry : newSettings.entrySet()) {
            setSetting(entry.getKey(), entry.getValue());
        }
        return getAllSettings();
    }
}
