package main.api;

import lombok.Getter;
import lombok.Setter;

public class RegisterErrors {

  @Getter
  @Setter
  private String email;

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String password;

  @Getter
  @Setter
  private String captcha;
}