package main.api.response;

import lombok.Getter;
import lombok.Setter;
import main.api.RegisterErrors;

public class FailRegisterResponse extends RegisterResponse {

  @Getter
  @Setter
  private RegisterErrors errors;
}