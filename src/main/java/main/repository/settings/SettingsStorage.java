package main.repository.settings;

import java.util.Optional;
import main.model.GlobalSetting;
import org.springframework.stereotype.Service;

@Service("SettingsStorage")
public class SettingsStorage implements SettingsRepository {

  private final PostgresSettingsRepository settingsRepository;

  public SettingsStorage(PostgresSettingsRepository settingsRepository) {
    this.settingsRepository = settingsRepository;
  }

  @Override
  public GlobalSetting getSetting(int settingId) {
    Optional<GlobalSetting> setting = settingsRepository.findById(settingId);
    return setting.orElse(null);
  }

  @Override
  public int setSetting(GlobalSetting setting) {
    settingsRepository.save(setting);
    return setting.getId();
  }
}