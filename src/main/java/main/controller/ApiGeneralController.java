package main.controller;

import main.api.response.CalendarResponse;
import main.api.response.InitResponse;
import main.api.response.SettingsResponse;
import main.api.response.TagResponse;
import main.service.calendar.CalendarServiceImpl;
import main.service.settings.SettingsServiceImpl;
import main.service.tags.TagsServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiGeneralController {

  private final InitResponse initResponse;
  private final SettingsServiceImpl settingsService;
  private final TagsServiceImpl tagsService;
  private final CalendarServiceImpl calendarService;


  public ApiGeneralController(InitResponse initResponse,
      SettingsServiceImpl settingsService, TagsServiceImpl tagsService,
      CalendarServiceImpl calendarService) {
    this.initResponse = initResponse;
    this.settingsService = settingsService;
    this.tagsService = tagsService;
    this.calendarService = calendarService;
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
  private TagResponse tags(@RequestParam(required = false) String query) {
    if (query == null) {
      return tagsService.getTags();
    }
    return tagsService.getTag(query);
  }

  @GetMapping("/calendar")
  private CalendarResponse calendar(@RequestParam(required = false, defaultValue = "0") int year) {
    return calendarService.getCalendar(year);
  }
}