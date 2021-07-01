package main.controller;

import java.security.Principal;
import main.api.request.AddCommentRequest;
import main.api.response.CalendarResponse;
import main.api.response.InitResponse;
import main.api.response.SettingsResponse;
import main.api.response.TagResponse;
import main.service.calendar.CalendarServiceImpl;
import main.service.image.ImageServiceImpl;
import main.service.posts.PostsServiceImpl;
import main.service.settings.SettingsServiceImpl;
import main.service.tags.TagsServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ApiGeneralController {

  private final InitResponse initResponse;
  private final SettingsServiceImpl settingsService;
  private final TagsServiceImpl tagsService;
  private final CalendarServiceImpl calendarService;
  private final ImageServiceImpl imageService;
  private final PostsServiceImpl postsService;


  public ApiGeneralController(
      InitResponse initResponse, SettingsServiceImpl settingsService,
      TagsServiceImpl tagsService, CalendarServiceImpl calendarService,
      ImageServiceImpl imageService, PostsServiceImpl postsService) {

    this.initResponse = initResponse;
    this.settingsService = settingsService;
    this.tagsService = tagsService;
    this.calendarService = calendarService;
    this.imageService = imageService;
    this.postsService = postsService;
  }


  @GetMapping("/init")
  public InitResponse init() {
    return initResponse;
  }

  @GetMapping("/settings")
  public SettingsResponse settings() {
    return settingsService.getGlobalSettings();
  }

  @GetMapping("/tag")
  public TagResponse tags(@RequestParam(required = false) String query) {
    if (query == null) {
      return tagsService.getTags();
    }
    return tagsService.getTag(query);
  }

  @GetMapping("/calendar")
  public CalendarResponse calendar(@RequestParam(required = false, defaultValue = "0") int year) {
    return calendarService.getCalendar(year);
  }

  @PostMapping(value = "/image", consumes = "multipart/form-data")
  @PreAuthorize("hasAuthority('use')")
  public ResponseEntity<?> addImage(@RequestParam("image") MultipartFile file) {
    return imageService.addImage(file);
  }

  @PostMapping(value = "/comment", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('use')")
  public ResponseEntity<?> addComment(
      @RequestBody AddCommentRequest addCommentRequest, Principal principal) {

    return postsService.addComment(
        addCommentRequest.getParentId(),
        addCommentRequest.getPostId(),
        addCommentRequest.getText(),
        principal);
  }
}