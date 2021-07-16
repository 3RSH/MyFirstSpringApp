package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class RestoreRequest {

  @Getter
  @Setter
  String code;

  @Getter
  @Setter
  String password;

  @Getter
  @Setter
  String captcha;

  @Getter
  @Setter
  @JsonProperty("captcha_secret")
  String captchaSecret;
}