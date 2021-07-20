package main.controller;

import java.security.Principal;
import java.util.Map;
import main.api.request.RestoreRequest;
import main.api.response.CaptchaResponse;
import main.api.response.CheckLoginResponse;
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
  public CheckLoginResponse check(Principal principal) {
    return loginService.getCheckResponse(principal);
  }

  @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
  public CheckLoginResponse login(@RequestBody Map<String, String> loginRequest) {
    return loginService.getLoginResponse(loginRequest);
  }

  @GetMapping("/captcha")
  public CaptchaResponse getCaptcha() {
    return captchaService.getNewCaptcha();
  }

  @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> register(@RequestBody Map<String, String> registerRequest) {
    return settingsService.getSetting("MULTIUSER_MODE")
        ? new ResponseEntity<>(userService.addUser(registerRequest), HttpStatus.OK)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @PostMapping(path = "/restore", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> restore(@RequestBody Map<String, String> restoreRequest) {
    return new ResponseEntity<>(
        userService.sendRestoreRequestUser(restoreRequest.get("email")),
        HttpStatus.OK);
  }

  @PostMapping(path = "/password", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> restore(@RequestBody RestoreRequest request) {
    return new ResponseEntity<>(userService.restoreUser(request), HttpStatus.OK);
  }
}