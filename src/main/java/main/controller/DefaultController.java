package main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultController {

  @RequestMapping("/")
  public String index() {
    return "index";
  }

  @RequestMapping(value = {"/posts/*", "/post/*", "/tag/*", "/search/*",
      "/calendar/*", "/calendar/*/*", "/login", "/login/*"})
  public String forwardToIndex() {
    return "forward:/";
  }
}