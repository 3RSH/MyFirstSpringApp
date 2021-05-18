package main.controller;

import java.io.IOException;
import main.api.response.CaptchaResponse;
import main.api.response.CheckResponse;
import main.service.captcha.CaptchaServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

  private final CheckResponse userStatus;
  private final CaptchaServiceImpl captchaService;


  public ApiAuthController(CheckResponse userStatus,
      CaptchaServiceImpl captchaService) {
    this.userStatus = userStatus;
    this.captchaService = captchaService;
  }


  @GetMapping("/check")
  private CheckResponse check() {
    return userStatus;
  }

  @GetMapping("/captcha")
  private CaptchaResponse getCaptcha() throws IOException {
    return captchaService.getNewCaptcha();
  }
}