package main.api;

import java.util.Comparator;

public class SortPostPreview {

  public static Comparator<PostPreview> RECENT = (post1, post2) ->
      (int) (post2.getTimestamp() - post1.getTimestamp());

  public static Comparator<PostPreview> POPULAR = (post1, post2) ->
      post2.getCommentCount() - post1.getCommentCount();

  public static Comparator<PostPreview> BEST = (post1, post2) -> {
    int post1Rating = post1.getLikeCount() - post1.getDislikeCount();
    int post2Rating = post2.getLikeCount() - post2.getDislikeCount();
    return post2Rating - post1Rating;
  };

  public static Comparator<PostPreview> EARLY = RECENT.reversed();
}