package main.api.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import main.api.Comment;
import main.api.UserPreview;

public class PostResponse {

  @Getter
  @Setter
  private int id;

  @Getter
  @Setter
  private long timestamp;

  @Getter
  @Setter
  private boolean active;

  @Getter
  @Setter
  private UserPreview user;

  @Getter
  @Setter
  private String title;

  @Getter
  @Setter
  private String text;

  @Getter
  @Setter
  private int likeCount;

  @Getter
  @Setter
  private int dislikeCount;

  @Getter
  @Setter
  private int viewCount;

  @Getter
  @Setter
  private List<Comment> comments;

  @Getter
  @Setter
  private List<String> tags;
}