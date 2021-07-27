package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class LoginRequest {

  @Getter
  @Setter
  @JsonProperty("e_mail")
  String email;

  @Getter
  @Setter
  String password;
}