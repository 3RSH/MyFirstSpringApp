package main.api.response;

import lombok.Getter;
import lombok.Setter;

public class UserResponse {

  @Getter
  @Setter
  private long id;

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String photo;

  @Getter
  @Setter
  private String email;

  @Getter
  @Setter
  private boolean moderation;

  @Getter
  @Setter
  private int moderationCount;

  @Getter
  @Setter
  private boolean setting;
}