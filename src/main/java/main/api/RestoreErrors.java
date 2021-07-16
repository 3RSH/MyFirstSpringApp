package main.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;

public class RestoreErrors {

  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  private String code;

  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  private String password;

  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  private String captcha;
}