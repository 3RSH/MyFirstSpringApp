package main.service.posts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import main.api.PostPreview;
import main.api.UserPreview;
import main.api.response.PostPreviewResponse;
import main.model.Post;
import main.repository.posts.PostsRepository;
import main.repository.votes.VotesRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    List<PostPreview> postPreviews = new ArrayList<>();
    long postCount;
    Pageable page = PageRequest.of(offset / limit, limit);
    Page<Post> postPage;

    switch (mode) {
      case ("popular"):
        postPage = postsRepository.findPopularPosts(page);
        break;

      case ("best"):
        postPage = postsRepository.findBestPosts(page);
        break;

      case ("early"):
        postPage = postsRepository.findEarlyPosts(page);
        break;

      default:
        postPage = postsRepository.findRecentPosts(page);
    }

    postCount = postPage.getTotalElements();

    for (Post post : postPage) {
      postPreviews.add(getPostPreview(post));
    }

    PostPreviewResponse postPreviewResponse = new PostPreviewResponse();

    postPreviewResponse.setCount((int) postCount);
    postPreviewResponse.setPosts(postPreviews);

    return postPreviewResponse;
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
    String text = post.getText().replaceAll("<.*>", "");

    if (text.length() > 150) {
      announce.append(text, 0, 50).append("\n")
          .append(text, 50, 100).append("\n")
          .append(text, 100, 150).append("...");
    } else if (text.length() > 100) {
      announce.append(text, 0, 50).append("\n")
          .append(text, 50, 100).append("\n")
          .append(text.substring(100));
    } else if (text.length() > 50) {
      announce.append(text, 0, 50).append("\n")
          .append(text.substring(50));
    } else {
      return text;
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