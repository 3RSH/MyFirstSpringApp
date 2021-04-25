package main.repository.settings;

import main.model.GlobalSetting;

public interface SettingsRepository {

  GlobalSetting getSetting(int settingId);
}