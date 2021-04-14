package main.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultController {

  @Value("${version}")
  private String appVersion;

  @RequestMapping("/")
  public String index() {
    return "index";
  }
}
