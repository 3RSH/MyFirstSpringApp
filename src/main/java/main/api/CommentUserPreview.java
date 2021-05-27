package main.api;

import lombok.Getter;
import lombok.Setter;

public class CommentUserPreview {

  @Getter
  @Setter
  private int id;

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String photo;
}