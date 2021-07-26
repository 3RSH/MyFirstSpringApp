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

  private static final String MULTIUSER_SETTING_CODE = "MULTIUSER_MODE";
  private static final String MODERATION_SETTING_CODE = "POST_PREMODERATION";
  private static final String STATISTIC_SETTING_CODE = "STATISTICS_IS_PUBLIC";
  private static final String SETTING_YES_VALUE = "YES";
  private static final String SETTING_NO_VALUE = "NO";

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
        case (MULTIUSER_SETTING_CODE):
          settings.setMultiuserMode(setting.getValue().equals(SETTING_YES_VALUE));
          break;

        case (MODERATION_SETTING_CODE):
          settings.setPostPremoderation(setting.getValue().equals(SETTING_YES_VALUE));
          break;

        case (STATISTIC_SETTING_CODE):
          settings.setStatisticsIsPublic(setting.getValue().equals(SETTING_YES_VALUE));
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
        case (MULTIUSER_SETTING_CODE):
          setting.setValue(request.isMultiuserMode() ? SETTING_YES_VALUE : SETTING_NO_VALUE);
          repository.save(setting);
          break;

        case (MODERATION_SETTING_CODE):
          setting.setValue(request.isPostPremoderation() ? SETTING_YES_VALUE : SETTING_NO_VALUE);
          repository.save(setting);
          break;

        case (STATISTIC_SETTING_CODE):
          setting.setValue(request.isStatisticsIsPublic() ? SETTING_YES_VALUE : SETTING_NO_VALUE);
          repository.save(setting);
          break;
      }
    }

    repository.flush();
  }

  @Override
  public boolean getSetting(String code) {
    GlobalSetting setting = repository.findFirstByCodeContaining(code);
    return setting != null && setting.getValue().equals(SETTING_YES_VALUE);
  }
}