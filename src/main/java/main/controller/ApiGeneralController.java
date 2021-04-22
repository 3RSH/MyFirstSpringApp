package main.controller;

import main.api.response.InitResponse;
import main.api.response.SettingsResponse;
import main.service.settings.SettingsServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiGeneralController {

  private final InitResponse initResponse;
  private final SettingsServiceImpl settingsService;

  public ApiGeneralController(InitResponse initResponse,
      SettingsServiceImpl settingsService) {
    this.initResponse = initResponse;
    this.settingsService = settingsService;
  }

  @GetMapping("/init")
  private InitResponse init() {
    return initResponse;
  }

  @GetMapping("/settings")
  private SettingsResponse settings() {
    return settingsService.getGlobalSettings();
  }
}