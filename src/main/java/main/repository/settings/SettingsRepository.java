package main.repository.settings;

import main.model.GlobalSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service("SettingsRepository")
@Repository
public interface SettingsRepository extends JpaRepository<GlobalSetting, Integer> {

  GlobalSetting findFirstByCodeContaining(String code);
}