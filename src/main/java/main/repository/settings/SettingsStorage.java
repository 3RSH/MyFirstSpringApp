package main.repository.settings;

import java.util.Optional;
import main.model.GlobalSetting;
import org.springframework.stereotype.Service;

@Service("SettingsStorage")
public class SettingsStorage implements SettingsRepository {

  private final PostgresSettingsRepository sqlSettingsRepository;

  public SettingsStorage(PostgresSettingsRepository sqlSettingsRepository) {
    this.sqlSettingsRepository = sqlSettingsRepository;
  }

  @Override
  public GlobalSetting getSetting(int settingId) {
    Optional<GlobalSetting> setting = sqlSettingsRepository.findById(settingId);
    return setting.orElse(null);
  }
}