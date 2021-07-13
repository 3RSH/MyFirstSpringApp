package main.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;

public class RegisterErrors {

  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  private String email;

  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  private String name;

  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  private String password;

  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  private String captcha;

  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  private String photo;
}