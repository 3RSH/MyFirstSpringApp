package main.api.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import main.api.PostPreview;

public class PostPreviewResponse {

  @Getter
  @Setter
  private int count;

  @Getter
  @Setter
  private List<PostPreview> posts;
}