package main.service.posts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import main.api.PostPreview;
import main.api.SortPostPreview;
import main.api.UserPreview;
import main.api.response.PostPreviewResponse;
import main.model.Post;
import main.model.Post.ModerationStatusType;
import main.repository.posts.PostsRepository;
import main.repository.votes.VotesRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PostsServiceImpl implements PostsService {

  private final PostsRepository postsRepository;
  private final VotesRepository votesRepository;


  public PostsServiceImpl(@Qualifier("PostsRepository") PostsRepository postsRepository,
      @Qualifier("VotesRepository") VotesRepository votesRepository) {
    this.votesRepository = votesRepository;
    this.postsRepository = postsRepository;
  }


  @Override
  public PostPreviewResponse getPostsPreview(int offset, int limit, String mode) {
    List<PostPreview> posts = new ArrayList<>();
    long postCount = postsRepository.count();

    for (int i = 1; i <= postCount; i++) {
      Post post = postsRepository.getOne(i);

      if (isAvailable(post)) {
        posts.add(getPostPreview(post));
      }
    }

    switch (mode) {
      case ("recent"):
        posts.sort(SortPostPreview.RECENT);
        break;

      case ("popular"):
        posts.sort(SortPostPreview.POPULAR);
        break;

      case ("best"):
        posts.sort(SortPostPreview.BEST);
        break;

      case ("early"):
        posts.sort(SortPostPreview.EARLY);
        break;
    }

    List<PostPreview> postsResponse = new ArrayList<>();

    for (int i = offset; i < (offset + limit); i++) {
      if (i == posts.size()) {
        break;
      }

      postsResponse.add(posts.get(i));
    }

    PostPreviewResponse postPreviewResponse = new PostPreviewResponse();

    postPreviewResponse.setCount(posts.size());
    postPreviewResponse.setPosts(postsResponse);

    return postPreviewResponse;
  }


  private boolean isAvailable(Post post) {
    return post.getIsActive() == 1
        && post.getModerationStatus() == ModerationStatusType.ACCEPTED
        && post.getTime().getTime() <= System.currentTimeMillis();
  }

  private PostPreview getPostPreview(Post post) {
    UserPreview userPreview = new UserPreview();

    userPreview.setId(post.getUser().getId());
    userPreview.setName(post.getUser().getName());

    PostPreview postPreview = new PostPreview();

    postPreview.setId(post.getId());
    postPreview.setTimestamp(post.getTime().getTime() / 1000);
    postPreview.setUser(userPreview);
    postPreview.setTitle(post.getTitle());
    postPreview.setAnnounce(getAnnounce(post));
    postPreview.setLikeCount(getLikesCountByPostId(post.getId()));
    postPreview.setDislikeCount(getDislikeCountByPostId(post.getId()));
    postPreview.setCommentCount(post.getComments().size());
    postPreview.setViewCount(post.getViewCount());

    return postPreview;
  }

  private String getAnnounce(Post post) {
    StringBuilder announce = new StringBuilder();

    if (post.getText().length() > 150) {
      announce.append(post.getText(), 0, 50).append("\n")
          .append(post.getText(), 50, 100).append("\n")
          .append(post.getText(), 100, 150).append("...");
    } else if (post.getText().length() > 100) {
      announce.append(post.getText(), 0, 50).append("\n")
          .append(post.getText(), 50, 100).append("\n")
          .append(post.getText().substring(100));
    } else if (post.getText().length() > 50) {
      announce.append(post.getText(), 0, 50).append("\n")
          .append(post.getText().substring(50));
    } else {
      return post.getText();
    }

    return announce.toString();
  }

  private int getLikesCountByPostId(int postId) {
    AtomicInteger count = new AtomicInteger();

    votesRepository.findAll().forEach(vote -> {
      if (vote.getPost().getId() == postId && vote.getValue() == 1) {
        count.getAndIncrement();
      }
    });

    return count.get();
  }

  private int getDislikeCountByPostId(int postId) {
    AtomicInteger count = new AtomicInteger();

    votesRepository.findAll().forEach(vote -> {
      if (vote.getPost().getId() == postId && vote.getValue() == -1) {
        count.getAndIncrement();
      }
    });

    return count.get();
  }
}