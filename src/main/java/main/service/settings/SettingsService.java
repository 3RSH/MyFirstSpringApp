package main.service.settings;

import java.util.Map;
import main.api.response.SettingsResponse;

public interface SettingsService {

  SettingsResponse getGlobalSettings();

  void setGlobalSettings(Map<String, Boolean> settings);

  boolean getSetting(String code);
}