package main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultController {

  @RequestMapping("/")
  public String index() {
    return "index";
  }

  @RequestMapping("/**")
  public String forwardToIndex() {
    return index();
  }
}