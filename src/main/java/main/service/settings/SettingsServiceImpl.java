package main.service.settings;

import main.api.response.SettingsResponse;
import main.repository.settings.SettingsRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SettingsServiceImpl implements SettingsService {

  private final SettingsRepository repository;


  public SettingsServiceImpl(@Qualifier("SettingsRepository") SettingsRepository repository) {
    this.repository = repository;
  }


  @Override
  public SettingsResponse getGlobalSettings() {
    SettingsResponse settings = new SettingsResponse();

    settings.setMultiuserMode(repository.getOne(1).getValue().equals("YES"));
    settings.setPostPremoderation(repository.getOne(2).getValue().equals("YES"));
    settings.setStatisticsIsPublic(repository.getOne(3).getValue().equals("YES"));

    return settings;
  }
}