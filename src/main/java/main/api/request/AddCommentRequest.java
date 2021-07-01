package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class AddCommentRequest {

  @Getter
  @Setter
  @JsonProperty("parent_id")
  int parentId;

  @Getter
  @Setter
  @JsonProperty("post_id")
  int postId;

  @Getter
  @Setter
  String text;
}