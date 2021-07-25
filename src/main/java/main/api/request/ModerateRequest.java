package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class ModerateRequest {

  @Getter
  @Setter
  @JsonProperty("post_id")
  int postId;

  @Getter
  @Setter
  String decision;
}