package main.controller;

import java.util.Map;
import main.api.response.CaptchaResponse;
import main.api.response.CheckResponse;
import main.api.response.RegisterResponse;
import main.service.captcha.CaptchaServiceImpl;
import main.service.user.UserServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

  private final CheckResponse userStatus;
  private final CaptchaServiceImpl captchaService;
  private final UserServiceImpl userService;


  public ApiAuthController(CheckResponse userStatus,
      CaptchaServiceImpl captchaService, UserServiceImpl userService) {
    this.userStatus = userStatus;
    this.captchaService = captchaService;
    this.userService = userService;
  }


  @GetMapping("/check")
  private CheckResponse check() {
    return userStatus;
  }

  @GetMapping("/captcha")
  private CaptchaResponse getCaptcha() {
    return captchaService.getNewCaptcha();
  }

  @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
  private RegisterResponse register(@RequestBody Map<String, String> registerRequest) {
    return userService.addUser(registerRequest);
  }
}