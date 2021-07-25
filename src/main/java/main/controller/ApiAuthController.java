package main.controller;

import java.security.Principal;
import main.api.request.GetRestoreRequest;
import main.api.request.LoginRequest;
import main.api.request.RegisterRequest;
import main.api.request.RestoreRequest;
import main.api.response.CaptchaResponse;
import main.api.response.CheckLoginResponse;
import main.api.response.RestoreResponse;
import main.service.captcha.CaptchaServiceImpl;
import main.service.login.LoginServiceImpl;
import main.service.settings.SettingsServiceImpl;
import main.service.user.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

  private final CaptchaServiceImpl captchaService;
  private final UserServiceImpl userService;
  private final LoginServiceImpl loginService;
  private final SettingsServiceImpl settingsService;


  public ApiAuthController(CaptchaServiceImpl captchaService, UserServiceImpl userService,
      LoginServiceImpl loginService, SettingsServiceImpl settingsService) {
    this.captchaService = captchaService;
    this.userService = userService;
    this.loginService = loginService;
    this.settingsService = settingsService;
  }


  @GetMapping("/check")
  public ResponseEntity<CheckLoginResponse> check(Principal principal) {
    return new ResponseEntity<>(loginService.getCheckResponse(principal), HttpStatus.OK);
  }

  @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<CheckLoginResponse> login(@RequestBody LoginRequest request) {
    return new ResponseEntity<>(loginService.getLoginResponse(request), HttpStatus.OK);
  }

  @GetMapping("/captcha")
  public ResponseEntity<CaptchaResponse> getCaptcha() {
    return new ResponseEntity<>(captchaService.getNewCaptcha(), HttpStatus.OK);
  }

  @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
    return settingsService.getSetting("MULTIUSER_MODE")
        ? new ResponseEntity<>(userService.addUser(request), HttpStatus.OK)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @PostMapping(path = "/restore", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<RestoreResponse> getRestore(@RequestBody GetRestoreRequest request) {
    return new ResponseEntity<>(userService.sendRestoreRequestUser(request), HttpStatus.OK);
  }

  @PostMapping(path = "/password", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<RestoreResponse> restore(@RequestBody RestoreRequest request) {
    return new ResponseEntity<>(userService.restoreUser(request), HttpStatus.OK);
  }
}