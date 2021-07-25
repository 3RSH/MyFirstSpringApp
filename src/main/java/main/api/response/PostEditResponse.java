package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import main.api.PostErrors;

public class PostEditResponse {

  @Getter
  @Setter
  private boolean result = true;

  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  private CommentResponse commentResponse;

  @Getter
  @Setter
  @JsonProperty("errors")
  @JsonInclude(Include.NON_NULL)
  private PostErrors creationErrors;
}