package main.service.posts;

import java.security.Principal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import main.api.Comment;
import main.api.CommentUserPreview;
import main.api.PostErrors;
import main.api.PostPreview;
import main.api.UserPreview;
import main.api.response.PostEditResponse;
import main.api.response.PostPreviewResponse;
import main.api.response.PostResponse;
import main.model.Post;
import main.model.Post.ModerationStatusType;
import main.model.PostComment;
import main.model.Tag;
import main.model.TagBinding;
import main.model.User;
import main.repository.posts.PostsRepository;
import main.repository.tags.TagBindingsRepository;
import main.repository.tags.TagsRepository;
import main.repository.users.UsersRepository;
import main.repository.votes.VotesRepository;
import main.security.Permission;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PostsServiceImpl implements PostsService {

  private final PostsRepository postsRepository;
  private final VotesRepository votesRepository;
  private final UsersRepository usersRepository;
  private final TagsRepository tagsRepository;
  private final TagBindingsRepository tagBindingsRepository;


  public PostsServiceImpl(
      @Qualifier("PostsRepository") PostsRepository postsRepository,
      @Qualifier("VotesRepository") VotesRepository votesRepository,
      @Qualifier("UsersRepository") UsersRepository usersRepository,
      @Qualifier("TagsRepository") TagsRepository tagsRepository,
      @Qualifier("TagBindingsRepository") TagBindingsRepository tagBindingsRepository) {
    this.votesRepository = votesRepository;
    this.postsRepository = postsRepository;
    this.usersRepository = usersRepository;
    this.tagsRepository = tagsRepository;
    this.tagBindingsRepository = tagBindingsRepository;
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
      int offset, int limit, String status) {
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

    updateViewCount(post);

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

  @Override
  public PostEditResponse addPost(
      long timestamp, short active, String title,
      List<String> tags, String text, Principal principal) {

    PostEditResponse creationResponse = new PostEditResponse();
    PostErrors creationErrors = getErrors(title, text, creationResponse);

    if (!creationResponse.isResult()) {
      creationResponse.setCreationErrors(creationErrors);
      return creationResponse;
    }

    Post post = new Post();
    User user = usersRepository.findFirstByEmail(principal.getName());

    post.setUser(user);
    post.setIsActive(active);

    long currentTime = System.currentTimeMillis();

    post.setTime(timestamp < currentTime / 1000
        ? new Timestamp(currentTime)
        : new Timestamp(timestamp * 1000));
    post.setTitle(title);
    post.setText(text);

    postsRepository.saveAndFlush(post);

    for (String tagName : tags) {
      Tag tag = tagsRepository.findFirstByName(tagName.toLowerCase());

      if (tag == null) {
        tag = new Tag();

        tag.setName(tagName.toLowerCase());
        tagsRepository.saveAndFlush(tag);
      }

      tagBindingsRepository.saveAndFlush(getTagBinding(tag, post));
    }

    return creationResponse;
  }

  @Override
  public PostEditResponse editPost(
      int id, long timestamp, short active, String title, List<String> tags, String text) {

    PostEditResponse editResponse = new PostEditResponse();
    PostErrors creationErrors = getErrors(title, text, editResponse);

    if (!editResponse.isResult()) {
      editResponse.setCreationErrors(creationErrors);
      return editResponse;
    }

    Post post = postsRepository.findPostsById(id);

    if (userHasNotRightToEdit(post)) {
      return editResponse;
    }

    post.setIsActive(active);

    long currentTime = System.currentTimeMillis();

    post.setTime(timestamp < currentTime / 1000
        ? new Timestamp(currentTime)
        : new Timestamp(timestamp * 1000));
    post.setTitle(title);
    post.setText(text);

    if (isOwner(post)) {
      post.setModerationStatus(ModerationStatusType.NEW);
    }

    postsRepository.saveAndFlush(post);
    updateTags(post, tags);

    return editResponse;
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

  private void updateViewCount(Post post) {

    if (userHasNotRightToEdit(post)) {
      post.setViewCount(post.getViewCount() + 1);
      postsRepository.saveAndFlush(post);
    }
  }

  private PostErrors getErrors(String title, String text, PostEditResponse response) {
    PostErrors errors = new PostErrors();

    if (title.length() < 3) {
      errors.setTitle("Заголовок не установлен");
      response.setResult(false);
    }

    if (text.length() < 50) {
      errors.setText("Текст публикации слишком короткий");
      response.setResult(false);
    }

    return errors;
  }

  private boolean userHasNotRightToEdit(Post post) {
    SecurityContext currentContext = SecurityContextHolder.getContext();
    SimpleGrantedAuthority authority
        = new SimpleGrantedAuthority(Permission.MODERATE.getPermission());

    boolean isModerator = currentContext
        .getAuthentication().getAuthorities().contains(authority);

    return !isOwner(post) && !isModerator;
  }

  private boolean isOwner(Post post) {
    SecurityContext currentContext = SecurityContextHolder.getContext();

    return post.getUser().getEmail()
        .equals(currentContext.getAuthentication().getName());
  }

  private TagBinding getTagBinding(Tag tag, Post post) {
    TagBinding tagBinding = new TagBinding();

    tagBinding.setTag(tag);
    tagBinding.setPost(post);

    return tagBinding;
  }

  private void updateTags(Post post, List<String> tags) {
    List<String> currentTagNames =
        post.getTags().stream().map(Tag::getName).collect(Collectors.toList());

    List<Tag> tagsForCheck = new ArrayList<>();

    for (String currentTagName : currentTagNames) {
      boolean mustDeleted = true;

      for (String tagName : tags) {
        if (tagName.equalsIgnoreCase(currentTagName)) {
          mustDeleted = false;
          break;
        }
      }

      if (mustDeleted) {
        Tag tag = tagsRepository.findFirstByName(currentTagName);
        TagBinding tagBinding = tagBindingsRepository.findFirstByTagAndPost(tag, post);

        tagBindingsRepository.delete(tagBinding);
        tagsForCheck.add(tag);
      }
    }

    for (String tagName : tags) {
      Tag tag = tagsRepository.findFirstByName(tagName.toLowerCase());

      if (tag == null) {
        tag = new Tag();

        tag.setName(tagName.toLowerCase());
        tagsRepository.save(tag);
        tagBindingsRepository.save(getTagBinding(tag, post));
      } else {
        TagBinding tagBinding = tagBindingsRepository.findFirstByTagAndPost(tag, post);

        if (tagBinding == null) {
          tagBindingsRepository.save(getTagBinding(tag, post));
        }
      }
    }

    tagBindingsRepository.flush();

    for (Tag tag : tagsForCheck) {
      TagBinding tagBinding = tagBindingsRepository.findFirstByTag(tag);

      if (tagBinding == null) {
        tagsRepository.deleteByName(tag.getName());
      }
    }

    tagsRepository.flush();
  }
}