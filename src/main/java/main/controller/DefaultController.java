package main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultController {

  private static final String INDEX_PAGE_NAME = "index";
  private static final String FORWARD_TO_INDEX = "forward:/";

  @RequestMapping("/")
  public String index() {
    return INDEX_PAGE_NAME;
  }

  @RequestMapping("/**/{path:[^\\.]*}")
  public String forwardToIndex() {
    return FORWARD_TO_INDEX;
  }
}