package main.service.settings;

import java.util.List;
import main.api.request.SettingsRequest;
import main.api.response.SettingsResponse;
import main.model.GlobalSetting;
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
    List<GlobalSetting> allSettings = repository.findAll();

    for (GlobalSetting setting : allSettings) {

      switch (setting.getCode()) {
        case ("MULTIUSER_MODE"):
          settings.setMultiuserMode(setting.getValue().equals("YES"));
          break;

        case ("POST_PREMODERATION"):
          settings.setPostPremoderation(setting.getValue().equals("YES"));
          break;

        case ("STATISTICS_IS_PUBLIC"):
          settings.setStatisticsIsPublic(setting.getValue().equals("YES"));
          break;
      }
    }

    return settings;
  }

  @Override
  public synchronized void setGlobalSettings(SettingsRequest request) {
    List<GlobalSetting> settings = repository.findAll();

    for (GlobalSetting setting : settings) {

      switch (setting.getCode()) {
        case ("MULTIUSER_MODE"):
          setting.setValue(request.isMultiuserMode() ? "YES" : "NO");
          repository.save(setting);
          break;

        case ("POST_PREMODERATION"):
          setting.setValue(request.isPostPremoderation() ? "YES" : "NO");
          repository.save(setting);
          break;

        case ("STATISTICS_IS_PUBLIC"):
          setting.setValue(request.isStatisticsIsPublic() ? "YES" : "NO");
          repository.save(setting);
          break;
      }
    }

    repository.flush();
  }

  @Override
  public boolean getSetting(String code) {
    GlobalSetting setting = repository.findFirstByCodeContaining(code);
    return setting != null && setting.getValue().equals("YES");
  }
}