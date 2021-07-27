package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class AddVoteRequest {

  @Getter
  @Setter
  @JsonProperty("post_id")
  int postId;
}