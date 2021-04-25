package main.controller;

import main.api.response.InitResponse;
import main.api.response.SettingsResponse;
import main.api.response.TagResponse;
import main.service.settings.SettingsServiceImpl;
import main.service.tags.TagsServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiGeneralController {

  private final InitResponse initResponse;
  private final SettingsServiceImpl settingsService;
  private final TagsServiceImpl tagsService;

  public ApiGeneralController(InitResponse initResponse,
      SettingsServiceImpl settingsService, TagsServiceImpl tagsService) {
    this.initResponse = initResponse;
    this.settingsService = settingsService;
    this.tagsService = tagsService;
  }

  @GetMapping("/init")
  private InitResponse init() {
    return initResponse;
  }

  @GetMapping("/settings")
  private SettingsResponse settings() {
    return settingsService.getGlobalSettings();
  }

  @GetMapping("/tag")
  private TagResponse tags() {
    return tagsService.getTags();
  }
}