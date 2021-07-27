package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import main.api.RegisterErrors;

public class RegisterResponse {

  @Getter
  @Setter
  private boolean result = true;

  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  private RegisterErrors errors;
}