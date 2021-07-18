package main.service.posts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import main.api.Comment;
import main.api.CommentUserPreview;
import main.api.PostErrors;
import main.api.PostPreview;
import main.api.UserPreview;
import main.api.response.CommentResponse;
import main.api.response.PostEditResponse;
import main.api.response.PostPreviewResponse;
import main.api.response.PostResponse;
import main.api.response.StatisticsResponse;
import main.api.response.VoteResponse;
import main.model.GlobalSetting;
import main.model.Post;
import main.model.Post.ModerationStatusType;
import main.model.PostComment;
import main.model.Tag;
import main.model.TagBinding;
import main.model.User;
import main.model.Vote;
import main.repository.comments.CommentsRepository;
import main.repository.posts.PostsRepository;
import main.repository.settings.SettingsRepository;
import main.repository.tags.TagBindingsRepository;
import main.repository.tags.TagsRepository;
import main.repository.users.UsersRepository;
import main.repository.votes.VotesRepository;
import main.security.Permission;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  private final CommentsRepository commentsRepository;
  private final SettingsRepository settingsRepository;


  public PostsServiceImpl(
      @Qualifier("PostsRepository") PostsRepository postsRepository,
      @Qualifier("VotesRepository") VotesRepository votesRepository,
      @Qualifier("UsersRepository") UsersRepository usersRepository,
      @Qualifier("TagsRepository") TagsRepository tagsRepository,
      @Qualifier("TagBindingsRepository") TagBindingsRepository tagBindingsRepository,
      @Qualifier("CommentsRepository") CommentsRepository commentsRepository,
      @Qualifier("SettingsRepository") SettingsRepository settingsRepository) {
    this.votesRepository = votesRepository;
    this.postsRepository = postsRepository;
    this.usersRepository = usersRepository;
    this.tagsRepository = tagsRepository;
    this.tagBindingsRepository = tagBindingsRepository;
    this.commentsRepository = commentsRepository;
    this.settingsRepository = settingsRepository;
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
  public PostPreviewResponse getMyPostsPreview(int offset, int limit, String status) {
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
    Post post = postsRepository.findPostById(id);
    PostResponse postResponse = new PostResponse();

    if (post == null ||
        (!post.getModerationStatus().equals(ModerationStatusType.ACCEPTED) &&
            userHasNotRightToEdit(post))) {

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
  public synchronized PostEditResponse addPost(
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

    post.setText("");
    text = uploadingImages(text, post);

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
  public synchronized PostEditResponse editPost(
      int id, long timestamp, short active, String title, List<String> tags, String text) {

    PostEditResponse editResponse = new PostEditResponse();
    Post post = postsRepository.findPostById(id);

    if (userHasNotRightToEdit(post)) {
      return editResponse;
    }

    PostErrors creationErrors = getErrors(title, text, editResponse);

    if (!editResponse.isResult()) {
      editResponse.setCreationErrors(creationErrors);
      return editResponse;
    }

    post.setIsActive(active);

    long currentTime = System.currentTimeMillis();

    post.setTime(timestamp < currentTime / 1000
        ? new Timestamp(currentTime)
        : new Timestamp(timestamp * 1000));

    post.setTitle(title);

    text = uploadingImages(text, post);

    post.setText(text);

    updateModerationStatus(post);

    postsRepository.saveAndFlush(post);
    votesRepository.flush();
    updateTags(post, tags);

    return editResponse;
  }

  @Override
  public synchronized ResponseEntity<?> addComment(
      int parentId, int postId, String text, Principal principal) {

    Post post = postsRepository.findPostById(postId);

    if ((post == null) || (!post.getModerationStatus().equals(ModerationStatusType.ACCEPTED)) ||
        (parentId != 0 &&
            post.getComments().stream().noneMatch(comment -> comment.getId() == parentId))) {

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    PostEditResponse creationResponse = new PostEditResponse();
    PostErrors creationErrors = getErrors(text, creationResponse);

    if (!creationResponse.isResult()) {
      creationResponse.setCreationErrors(creationErrors);
      return new ResponseEntity<>(creationResponse, HttpStatus.BAD_REQUEST);
    }

    PostComment comment = new PostComment();
    User user = usersRepository.findFirstByEmail(principal.getName());

    comment.setUser(user);
    comment.setPost(post);

    if (parentId != 0) {
      comment.setParentComment(commentsRepository.getOne(parentId));
    }

    long currentTime = System.currentTimeMillis();

    comment.setTime(new Timestamp(currentTime));
    comment.setText("");

    text = uploadingImages(text);

    comment.setText(text);

    commentsRepository.saveAndFlush(comment);

    CommentResponse response = new CommentResponse();

    response.setId(comment.getId());

    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @Override
  public synchronized VoteResponse addVote(int postId, short voteValue) {
    VoteResponse response = new VoteResponse();
    User user = getCurrentUser();
    Post post = postsRepository.findPostById(postId);

    if (!post.getModerationStatus().equals(ModerationStatusType.ACCEPTED)) {
      return response;
    }

    if (isOwner(post) || isModerator()) {
      return response;
    }

    Vote vote = votesRepository.findFirstByPostAndUser(post, user);

    if (vote != null) {

      if (vote.getValue() == voteValue) {
        return response;
      }

      vote.setValue(voteValue);
      response.setResult(true);
      vote.setTime(new Timestamp(System.currentTimeMillis()));
      votesRepository.saveAndFlush(vote);

      return response;
    }

    vote = new Vote();
    vote.setPost(post);
    vote.setUser(user);
    vote.setTime(new Timestamp(System.currentTimeMillis()));
    votesRepository.saveAndFlush(vote);

    return response;
  }

  @Override
  public StatisticsResponse getMyStatistics() {
    User user = getCurrentUser();
    List<Post> posts = postsRepository.findPublishedPostsByUser(user.getId());

    return getStatistics(posts);
  }

  @Override
  public ResponseEntity<?> getAllStatistics() {
    GlobalSetting statisticsSetting =
        settingsRepository.findFirstByCodeContaining("STATISTICS_IS_PUBLIC");

    if (statisticsSetting.getValue().equals("NO") && !isModerator()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    List<Post> posts = postsRepository.findAll();

    return new ResponseEntity<>(getStatistics(posts), HttpStatus.OK);
  }

  @Override
  public PostPreviewResponse getModeratedPostsPreview(int offset, int limit, String status) {
    Pageable page = PageRequest.of(offset / limit, limit);
    int moderatorId = getCurrentUser().getId();

    switch (status) {
      case ("declined"):
        return getPostPreviewResponse(
            postsRepository.findDeclinedPostsByModerator(moderatorId, page));

      case ("accepted"):
        return getPostPreviewResponse(
            postsRepository.findAcceptedPostsByModerator(moderatorId, page));

      default:
        return getPostPreviewResponse(postsRepository.findNewModeratedPosts(page));
    }
  }

  @Override
  public synchronized PostEditResponse moderatePost(Map<String, String> moderateRequest) {
    PostEditResponse response = new PostEditResponse();

    Post post = postsRepository.findPostById(Integer.parseInt(moderateRequest.get("post_id")));

    if (post == null) {
      response.setResult(false);
      return response;
    }

    String decision = moderateRequest.get("decision");

    if (decision.equals("accept")) {
      post.setModerationStatus(ModerationStatusType.ACCEPTED);
    } else {
      post.setModerationStatus(ModerationStatusType.DECLINED);
    }

    post.setModerator(getCurrentUser());
    postsRepository.saveAndFlush(post);

    return response;
  }


  private StatisticsResponse getStatistics(List<Post> posts) {
    StatisticsResponse response = new StatisticsResponse();
    int likesCount = 0, dislikesCount = 0, viewsCount = 0;
    long firstPublication = System.currentTimeMillis() / 1000;

    for (Post post : posts) {
      likesCount += getLikesCountByPostId(post.getId());
      dislikesCount += getDislikeCountByPostId(post.getId());
      viewsCount += post.getViewCount();
      long publicationTime = post.getTime().getTime() / 1000;

      if (firstPublication > publicationTime) {
        firstPublication = publicationTime;
      }
    }

    response.setPostsCount(posts.size());
    response.setLikesCount(likesCount);
    response.setDislikesCount(dislikesCount);
    response.setViewsCount(viewsCount);
    response.setFirstPublication(firstPublication);

    return response;
  }

  private void updateModerationStatus(Post post) {
    if (isOwner(post) || isModerator()) {
      post.setModerationStatus(ModerationStatusType.NEW);
      post.setComments(new ArrayList<>());
      post.setViewCount(0);

      votesRepository.deleteAll(votesRepository.findAllByPost(post));
    }
  }

  private String uploadingImages(String text, Post post) {
    List<String> tempPaths = getPathsFromText(text);
    List<String> paths = new ArrayList<>(Collections.nCopies(tempPaths.size(), ""));

    Collections.copy(paths, tempPaths);

    for (int i = 0; i < paths.size(); i++) {
      String path = paths.get(i).replaceFirst("upload", "images");
      paths.set(i, path);
    }

    for (int i = 0; i < tempPaths.size(); i++) {
      text = text.replace(tempPaths.get(i), paths.get(i));
    }

    convertPathsForFile(tempPaths);
    convertPathsForFile(paths);

    copyFiles(tempPaths, paths);

    Path uploadPath = Paths.get("target/classes/static/upload");

    if (Files.exists(uploadPath)) {
      try {
        FileUtils.cleanDirectory(uploadPath.toFile());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    updateFiles(post, paths);

    return text;
  }

  private String uploadingImages(String text) {
    List<String> tempPaths = getPathsFromText(text);
    List<String> paths = new ArrayList<>(Collections.nCopies(tempPaths.size(), ""));

    Collections.copy(paths, tempPaths);

    for (int i = 0; i < paths.size(); i++) {
      String path = paths.get(i).replaceFirst("upload", "images");
      paths.set(i, path);
    }

    for (int i = 0; i < tempPaths.size(); i++) {
      text = text.replace(tempPaths.get(i), paths.get(i));
    }

    convertPathsForFile(tempPaths);
    convertPathsForFile(paths);
    copyFiles(tempPaths, paths);

    return text;
  }

  private void updateFiles(Post post, List<String> paths) {
    String originalText = post.getText();
    List<String> oldPaths = getPathsFromText(originalText);

    convertPathsForFile(oldPaths);

    for (String path : paths) {
      oldPaths.remove(path);
    }

    deleteFiles(oldPaths);
  }

  private void deleteFiles(List<String> source) {
    for (String path : source) {
      path = path.substring(0, path.lastIndexOf("images/") + 9);
      File dir = new File(path);

      try {
        FileUtils.deleteDirectory(dir);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void copyFiles(List<String> source, List<String> destination) {
    for (int i = 0; i < source.size(); i++) {

      File sourceFile = new File(source.get(i));
      String destPath = destination.get(i);
      File destinationDir = new File(destPath.substring(0, destPath.lastIndexOf("/")));
      File destinationFile = new File(destination.get(i));

      if (destinationDir.mkdirs()) {

        try {
          FileUtils.copyFile(sourceFile, destinationFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void convertPathsForFile(List<String> paths) {
    for (int i = 0; i < paths.size(); i++) {
      String path = paths.get(i);

      path = path.substring(10, path.length() - 2);
      path = path.replaceAll("\\\\", "/");
      path = "target/classes/static" + path;

      paths.set(i, path);
    }
  }

  private List<String> getPathsFromText(String text) {
    List<String> paths = new ArrayList<>();
    Pattern pattern = Pattern.compile("<img src=\".+?\">");
    Matcher matcher = pattern.matcher(text);

    while (matcher.find()) {
      paths.add(matcher.group());
    }

    return paths;
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
    String text = post.getText().replaceAll("<.*?>", "");

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

  private PostErrors getErrors(String text, PostEditResponse response) {
    PostErrors errors = new PostErrors();

    if (text.length() < 3) {
      errors.setText("Текст комментария не задан или слишком короткий");
      response.setResult(false);
    }

    return errors;
  }

  private boolean userHasNotRightToEdit(Post post) {
    return !isOwner(post) && !isModerator();
  }

  private boolean isModerator() {
    SecurityContext currentContext = SecurityContextHolder.getContext();
    SimpleGrantedAuthority authority
        = new SimpleGrantedAuthority(Permission.MODERATE.getPermission());

    return currentContext.getAuthentication().getAuthorities().contains(authority);
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

  private User getCurrentUser() {
    SecurityContext currentContext = SecurityContextHolder.getContext();
    String email = currentContext.getAuthentication().getName();
    return usersRepository.findFirstByEmail(email);
  }
}