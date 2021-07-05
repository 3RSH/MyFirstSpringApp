package main.api.response;

import lombok.Getter;
import lombok.Setter;

public class StatisticsResponse {

  @Getter
  @Setter
  private int postsCount;

  @Getter
  @Setter
  private int likesCount;

  @Getter
  @Setter
  private int dislikesCount;

  @Getter
  @Setter
  private int viewsCount;

  @Getter
  @Setter
  private long firstPublication;
}