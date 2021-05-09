package main.api;

import lombok.Getter;
import lombok.Setter;

public class PostPreview {

  @Getter
  @Setter
  private int id;

  @Getter
  @Setter
  private long timestamp;

  @Getter
  @Setter
  private UserPreview user;

  @Getter
  @Setter
  private String title;

  @Getter
  @Setter
  private String announce;

  @Getter
  @Setter
  private int likeCount;

  @Getter
  @Setter
  private int dislikeCount;

  @Getter
  @Setter
  private int commentCount;

  @Getter
  @Setter
  private int viewCount;
}