package main.api.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import main.api.TagInfo;

public class TagResponse {

  private String title = "tags";

  @Getter
  @Setter
  private List<TagInfo> tags;

}
