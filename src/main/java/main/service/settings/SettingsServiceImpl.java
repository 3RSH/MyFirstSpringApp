package main.service.settings;

import main.api.response.SettingsResponse;
import main.repository.settings.SettingsRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SettingsServiceImpl implements SettingsService {

  private final SettingsRepository storage;


  public SettingsServiceImpl(@Qualifier("SettingsStorage") SettingsRepository storage) {
    this.storage = storage;
  }


  @Override
  public SettingsResponse getGlobalSettings() {
    SettingsResponse settings = new SettingsResponse();

    settings.setMultiuserMode(storage.getSetting(1).getValue().equals("YES"));
    settings.setPostPremoderation(storage.getSetting(2).getValue().equals("YES"));
    settings.setStatisticsIsPublic(storage.getSetting(3).getValue().equals("YES"));

    return settings;
  }
}