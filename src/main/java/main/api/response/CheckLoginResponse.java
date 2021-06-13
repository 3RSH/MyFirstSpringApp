package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class CheckLoginResponse {

  @Getter
  @Setter
  private boolean result;

  @Getter
  @Setter
  @JsonProperty("user")
  @JsonInclude(Include.NON_NULL)
  private UserResponse userResponse;
}