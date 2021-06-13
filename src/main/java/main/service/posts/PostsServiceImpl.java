package main.service.posts;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import main.api.Comment;
import main.api.CommentUserPreview;
import main.api.PostPreview;
import main.api.UserPreview;
import main.api.response.PostPreviewResponse;
import main.api.response.PostResponse;
import main.model.Post;
import main.model.PostComment;
import main.model.Tag;
import main.repository.posts.PostsRepository;
import main.repository.users.UsersRepository;
import main.repository.votes.VotesRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PostsServiceImpl implements PostsService {

  private final PostsRepository postsRepository;
  private final VotesRepository votesRepository;
  private final UsersRepository usersRepository;


  public PostsServiceImpl(
      @Qualifier("PostsRepository") PostsRepository postsRepository,
      @Qualifier("VotesRepository") VotesRepository votesRepository,
      @Qualifier("UsersRepository") UsersRepository usersRepository) {
    this.votesRepository = votesRepository;
    this.postsRepository = postsRepository;
    this.usersRepository = usersRepository;
  }


  @Override
  public PostPreviewResponse getPostsPreview(int offset, int limit, String mode) {
    Pageable page = PageRequest.of(offset / limit, limit);

    switch (mode) {
      case ("popular"):
        return getPostPreviewResponse(postsRepository.findPopularPosts(page));

      case ("best"):
        return getPostPreviewResponse(postsRepository.findBestPosts(page));

      case ("early"):
        return getPostPreviewResponse(postsRepository.findEarlyPosts(page));

      default:
        return getPostPreviewResponse(postsRepository.findRecentPosts(page));
    }
  }

  @Override
  public PostPreviewResponse getMyPostsPreview(
      int offset, int limit, String status, Principal principal) {
    Pageable page = PageRequest.of(offset / limit, limit);
    int userId = usersRepository.findFirstByEmail(
        SecurityContextHolder.getContext().getAuthentication().getName()).
        getId();

    switch (status) {
      case ("pending"):
        return getPostPreviewResponse(postsRepository.findPendingPostsByUser(userId, page));

      case ("declined"):
        return getPostPreviewResponse(postsRepository.findDeclinedPostsByUser(userId, page));

      case ("published"):
        return getPostPreviewResponse(postsRepository.findPublishedPostsByUser(userId, page));

      default:
        return getPostPreviewResponse(postsRepository.findInactivePostsByUser(userId, page));
    }
  }

  @Override
  public PostPreviewResponse getPostsPreviewByQuery(int offset, int limit, String query) {
    if (query.matches("\\s*")) {
      return getPostsPreview(offset, limit, "recent");
    }

    Pageable page = PageRequest.of(offset / limit, limit);

    return getPostPreviewResponse(postsRepository.findAllByTitleContainingIgnoreCase(query, page));
  }

  @Override
  public PostPreviewResponse getPostsPreviewByDate(int offset, int limit, String date) {
    Pageable page = PageRequest.of(offset / limit, limit);
    String[] dateParams = date.split("-");

    return getPostPreviewResponse(postsRepository.findPostsByDate(
        Integer.parseInt(dateParams[0]),
        Integer.parseInt(dateParams[1]),
        Integer.parseInt(dateParams[2]),
        page));
  }

  @Override
  public PostPreviewResponse getPostsPreviewByTag(int offset, int limit, String tag) {
    Pageable page = PageRequest.of(offset / limit, limit);

    return getPostPreviewResponse(postsRepository.findPostsByTag(tag, page));
  }

  @Override
  public PostResponse getPostById(int id) {
    Post post = postsRepository.findPostsById(id);
    PostResponse postResponse = new PostResponse();

    if (post == null) {
      return postResponse;
    }

    post.setViewCount(post.getViewCount() + 1);
    postsRepository.saveAndFlush(post);

    postResponse.setId(post.getId());
    postResponse.setTimestamp(post.getTime().getTime() / 1000);
    postResponse.setActive(post.getIsActive() == 1);

    UserPreview user = new UserPreview();

    user.setId(post.getUser().getId());
    user.setName(post.getUser().getName());

    postResponse.setUser(user);
    postResponse.setTitle(post.getTitle());
    postResponse.setText(post.getText());
    postResponse.setLikeCount(getLikesCountByPostId(post.getId()));
    postResponse.setDislikeCount(getDislikeCountByPostId(post.getId()));
    postResponse.setViewCount(post.getViewCount());

    List<Comment> comments = new ArrayList<>();

    for (PostComment pComment : post.getComments()) {
      Comment comment = new Comment();

      comment.setId(pComment.getId());
      comment.setTimestamp(pComment.getTime().getTime() / 1000);
      comment.setText(pComment.getText());

      CommentUserPreview cUser = new CommentUserPreview();

      cUser.setId(pComment.getUser().getId());
      cUser.setName(pComment.getUser().getName());
      cUser.setPhoto(pComment.getUser().getPhoto());

      comment.setUser(cUser);

      comments.add(comment);
    }

    postResponse.setComments(comments);

    List<String> tags = new ArrayList<>();

    for (Tag tag : post.getTags()) {
      tags.add(tag.getName());
    }

    postResponse.setTags(tags);

    return postResponse;
  }

  private PostPreviewResponse getPostPreviewResponse(Page<Post> postPage) {
    List<PostPreview> postPreviews = new ArrayList<>();
    long postCount = postPage.getTotalElements();

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