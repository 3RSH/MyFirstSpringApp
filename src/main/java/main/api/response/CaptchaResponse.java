package main.api.response;

import lombok.Getter;
import lombok.Setter;

public class CaptchaResponse {

  @Getter
  @Setter
  String secret;

  @Getter
  @Setter
  String image;
}