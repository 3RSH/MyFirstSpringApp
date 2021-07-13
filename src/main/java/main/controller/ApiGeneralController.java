package main.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import main.api.request.AddCommentRequest;
import main.api.response.CalendarResponse;
import main.api.response.InitResponse;
import main.api.response.SettingsResponse;
import main.api.response.StatisticsResponse;
import main.api.response.TagResponse;
import main.service.calendar.CalendarServiceImpl;
import main.service.image.ImageServiceImpl;
import main.service.posts.PostsServiceImpl;
import main.service.settings.SettingsServiceImpl;
import main.service.tags.TagsServiceImpl;
import main.service.user.UserServiceImpl;
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
  private final UserServiceImpl userService;


  public ApiGeneralController(
      InitResponse initResponse, SettingsServiceImpl settingsService,
      TagsServiceImpl tagsService, CalendarServiceImpl calendarService,
      ImageServiceImpl imageService, PostsServiceImpl postsService,
      UserServiceImpl userService) {

    this.initResponse = initResponse;
    this.settingsService = settingsService;
    this.tagsService = tagsService;
    this.calendarService = calendarService;
    this.imageService = imageService;
    this.postsService = postsService;
    this.userService = userService;
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

  @GetMapping("/statistics/my")
  @PreAuthorize("hasAuthority('use')")
  public StatisticsResponse myStatistics() {
    return postsService.getMyStatistics();
  }

  @GetMapping("/statistics/all")
  public ResponseEntity<?> allStatistics() {
    return postsService.getAllStatistics();
  }

  @PostMapping(value = "/profile/my", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('use')")
  public ResponseEntity<?> editProfile(@RequestBody Map<String, String> editRequest) {
    return userService.editUser(editRequest);
  }

  @PostMapping(value = "/profile/my", consumes = "multipart/form-data")
  @PreAuthorize("hasAuthority('use')")
  public ResponseEntity<?> editProfile(
      @RequestParam("photo") MultipartFile file,
      @RequestParam("name") String name,
      @RequestParam("email") String email,
      @RequestParam("removePhoto") String removePhoto,
      @RequestParam(name = "password", required = false) String password) {

    ResponseEntity<?> responseEntity = imageService.addAvatar(file);

    if (responseEntity.getStatusCode().isError()) {
      return responseEntity;
    }

    Map<String, String> request = new HashMap<>();

    request.put("name", name);
    request.put("email", email);
    request.put("password", password);
    request.put("removePhoto", removePhoto);
    request.put("photo", String.valueOf(responseEntity.getBody()));

    return userService.editUser(request);
  }
}