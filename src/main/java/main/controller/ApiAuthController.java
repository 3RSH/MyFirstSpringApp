package main.controller;

import java.security.Principal;
import java.util.Map;
import main.api.response.CaptchaResponse;
import main.api.response.CheckLoginResponse;
import main.api.response.RegisterResponse;
import main.service.captcha.CaptchaServiceImpl;
import main.service.login.LoginServiceImpl;
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

  private final CaptchaServiceImpl captchaService;
  private final UserServiceImpl userService;
  private final LoginServiceImpl loginService;


  public ApiAuthController(CaptchaServiceImpl captchaService,
      UserServiceImpl userService, LoginServiceImpl loginService) {
    this.captchaService = captchaService;
    this.userService = userService;
    this.loginService = loginService;
  }


  @GetMapping("/check")
  private CheckLoginResponse check(Principal principal) {
    return loginService.getCheckResponse(principal);
  }

  @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
  private CheckLoginResponse login(@RequestBody Map<String, String> loginRequest) {
    return loginService.getLoginResponse(loginRequest);
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