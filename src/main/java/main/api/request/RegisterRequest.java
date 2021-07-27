package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class RegisterRequest {

  @Getter
  @Setter
  @JsonProperty("e_mail")
  String email;

  @Getter
  @Setter
  String password;

  @Getter
  @Setter
  String name;

  @Getter
  @Setter
  String captcha;

  @Getter
  @Setter
  @JsonProperty("captcha_secret")
  String captchaSecret;
}