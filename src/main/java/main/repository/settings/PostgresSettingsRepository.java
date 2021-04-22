package main.repository.settings;

import main.model.GlobalSetting;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostgresSettingsRepository extends CrudRepository<GlobalSetting, Integer> {

}