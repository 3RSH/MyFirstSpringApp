package main.api;

import java.util.Comparator;

public class SortPostPreview {

  public static Comparator<PostPreview> RECENT = new Comparator<PostPreview>() {
    @Override
    public int compare(PostPreview post1, PostPreview post2) {
      return (int) (post2.getTimestamp() - post1.getTimestamp());
    }
  };

  public static Comparator<PostPreview> POPULAR = new Comparator<PostPreview>() {
    @Override
    public int compare(PostPreview post1, PostPreview post2) {
      return post2.getCommentCount() - post1.getCommentCount();
    }
  };

  public static Comparator<PostPreview> BEST = new Comparator<PostPreview>() {
    @Override
    public int compare(PostPreview post1, PostPreview post2) {
      int postStatus1 = post1.getLikeCount() - post1.getDislikeCount();
      int postStatus2 = post2.getLikeCount() - post2.getDislikeCount();
      return postStatus2 - postStatus1;
    }
  };

  public static Comparator<PostPreview> EARLY = new Comparator<PostPreview>() {
    @Override
    public int compare(PostPreview post1, PostPreview post2) {
      return (int) (post1.getTimestamp() - post2.getTimestamp());
    }
  };
}