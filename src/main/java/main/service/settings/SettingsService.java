package main.service.settings;

import main.api.request.SettingsRequest;
import main.api.response.SettingsResponse;

public interface SettingsService {

  SettingsResponse getGlobalSettings();

  void setGlobalSettings(SettingsRequest request);

  boolean getSetting(String code);
}