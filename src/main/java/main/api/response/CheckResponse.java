package main.api.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
public class CheckResponse {

  @Getter
  @Setter
  private boolean result;
}