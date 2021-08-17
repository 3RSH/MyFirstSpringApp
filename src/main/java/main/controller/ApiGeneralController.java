package main.controller;

import java.security.Principal;
import main.api.request.AddCommentRequest;
import main.api.request.EditProfileRequest;
import main.api.request.ModerateRequest;
import main.api.request.SettingsRequest;
import main.api.response.CalendarResponse;
import main.api.response.EditProfileResponse;
import main.api.response.ImageResponse;
import main.api.response.InitResponse;
import main.api.response.PostEditResponse;
import main.api.response.SettingsResponse;
import main.api.response.StatisticsResponse;
import main.api.response.TagResponse;
import main.service.calendar.CalendarServiceImpl;
import main.service.image.ImageServiceImpl;
import main.service.posts.PostsServiceImpl;
import main.service.settings.SettingsServiceImpl;
import main.service.tags.TagsServiceImpl;
import main.service.user.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ApiGeneralController {

  private static final String PHOTO_PARAMETER = "photo";
  private static final String NAME_PARAMETER = "name";
  private static final String EMAIL_PARAMETER = "email";
  private static final String PASSWORD_PARAMETER = "password";
  private static final String REMOVE_PHOTO_PARAMETER = "removePhoto";
  private static final String DEFAULT_INT_PARAMETER = "0";
  private static final String IMAGE_PARAMETER = "image";
  private static final String STATISTIC_SETTING_CODE = "STATISTICS_IS_PUBLIC";

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
  public ResponseEntity<InitResponse> init() {
    return new ResponseEntity<>(initResponse, HttpStatus.OK);
  }

  @GetMapping("/settings")
  public ResponseEntity<SettingsResponse> settings() {
    return new ResponseEntity<>(settingsService.getGlobalSettings(), HttpStatus.OK);
  }

  @GetMapping("/tag")
  public ResponseEntity<TagResponse> tags(@RequestParam(required = false) String query) {
    return query == null
        ? new ResponseEntity<>(tagsService.getTags(), HttpStatus.OK)
        : new ResponseEntity<>(tagsService.getTag(query), HttpStatus.OK);
  }

  @GetMapping("/calendar")
  public ResponseEntity<CalendarResponse> calendar(
      @RequestParam(required = false, defaultValue = DEFAULT_INT_PARAMETER) int year) {

    return new ResponseEntity<>(calendarService.getCalendar(year), HttpStatus.OK);
  }

  @PostMapping(value = "/image", consumes = "multipart/form-data")
  @PreAuthorize("hasAuthority('use')")
  public ResponseEntity<?> addImage(@RequestParam(IMAGE_PARAMETER) MultipartFile file) {

    synchronized (imageService) {
      ImageResponse response = imageService.addImage(file);

      return response.isResult()
          ? new ResponseEntity<>(response.getImagePath(), HttpStatus.OK)
          : new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
  }

  @PostMapping(value = "/comment", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('use')")
  public ResponseEntity<?> addComment(
      @RequestBody AddCommentRequest request, Principal principal) {

    PostEditResponse response = postsService.addComment(request, principal);

    if (response.isResult() && response.getCommentResponse() == null) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    return response.isResult()
        ? new ResponseEntity<>(response.getCommentResponse(), HttpStatus.OK)
        : new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @GetMapping("/statistics/my")
  @PreAuthorize("hasAuthority('use')")
  public ResponseEntity<StatisticsResponse> myStatistics() {
    return new ResponseEntity<>(postsService.getMyStatistics(), HttpStatus.OK);
  }

  @GetMapping("/statistics/all")
  public ResponseEntity<?> allStatistics() {
    StatisticsResponse response
        = postsService.getAllStatistics(settingsService.getSetting(STATISTIC_SETTING_CODE));

    return response.getPostsCount() != 0
        ? new ResponseEntity<>(response, HttpStatus.OK)
        : new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
  }

  @PostMapping(value = "/profile/my", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('use')")
  public ResponseEntity<EditProfileResponse> editProfile(
      @RequestBody EditProfileRequest request) {

    EditProfileResponse response = userService.editUser(request);

    return response.isResult()
        ? new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED)
        : new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @PostMapping(value = "/profile/my", consumes = "multipart/form-data")
  @PreAuthorize("hasAuthority('use')")
  public ResponseEntity<?> editProfile(
      @RequestParam(PHOTO_PARAMETER) MultipartFile file,
      @RequestParam(NAME_PARAMETER) String name,
      @RequestParam(EMAIL_PARAMETER) String email,
      @RequestParam(REMOVE_PHOTO_PARAMETER) short removePhoto,
      @RequestParam(name = PASSWORD_PARAMETER, required = false) String password) {

    synchronized (imageService) {

      ImageResponse imageResponse = imageService.addAvatar(file);

      if (!imageResponse.isResult()) {
        return new ResponseEntity<>(imageResponse, HttpStatus.BAD_REQUEST);
      }

      EditProfileRequest request = new EditProfileRequest();

      request.setName(name);
      request.setEmail(email);
      request.setPassword(password);
      request.setRemovePhoto(removePhoto);
      request.setPhoto(imageResponse.getImagePath());

      EditProfileResponse response = userService.editUser(request);

      return response.isResult()
          ? new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED)
          : new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
  }

  @PostMapping(value = "/moderation", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('moderate')")
  public ResponseEntity<PostEditResponse> moderatePost(@RequestBody ModerateRequest request) {
    return new ResponseEntity<>(postsService.moderatePost(request), HttpStatus.OK);
  }

  @PutMapping(value = "/settings", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('moderate')")
  public ResponseEntity<?> setSettings(@RequestBody SettingsRequest request) {
    settingsService.setGlobalSettings(request);

    return new ResponseEntity<>(HttpStatus.OK);
  }
}