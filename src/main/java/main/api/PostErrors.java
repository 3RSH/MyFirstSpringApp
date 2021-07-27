package main.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;

public class PostErrors {

  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  private String title;

  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  private String text;
}