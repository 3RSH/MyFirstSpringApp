package main.controller;

import main.api.response.CheckResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

  private final CheckResponse userStatus;

  public ApiAuthController(CheckResponse userStatus) {
    this.userStatus = userStatus;
  }

  @GetMapping("/check")
  private CheckResponse check() {
    return userStatus;
  }
}
