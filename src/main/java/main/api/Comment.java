package main.api;

import lombok.Getter;
import lombok.Setter;

public class Comment {

  @Getter
  @Setter
  private int id;

  @Getter
  @Setter
  private long timestamp;

  @Getter
  @Setter
  private String text;

  @Getter
  @Setter
  private CommentUserPreview user;
}