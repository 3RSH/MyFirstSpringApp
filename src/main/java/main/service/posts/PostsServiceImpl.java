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
import main.model.Post;
import main.model.Post.ModerationStatusType;
import main.model.PostComment;
import main.model.Tag;
import main.model.TagBinding;
import main.model.User;
import main.model.Vote;
import main.repository.comments.CommentsRepository;
import main.repository.posts.PostsRepository;
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

  private static final int YEAR_INDEX = 0;
  private static final int MONTH_INDEX = 1;
  private static final int DAY_INDEX = 2;
  private static final int TIME_DIVIDER = 1000;
  private static final short ACTIVE_POST_MARKER = 1;
  private static final int DEFAULT_VALUE = 0;
  private static final int IMAGE_PATH_OFFSET = 9;
  private static final int FILENAME_START_INDEX = 10;
  private static final int FILENAME_END_INDEX_OFFSET = 2;
  private static final int PREPOST_STRING_LENGTH = 50;
  private static final int PREPOST_STRING_COUNT = 3;
  private static final int LIKE_VALUE = 1;
  private static final int DISLIKE_VALUE = -1;
  private static final int MIN_TITLE_SIZE = 3;
  private static final int MIN_TEXT_SIZE = 50;
  private static final int MIN_COMMENT_SIZE = 3;

  private final PostsRepository postsRepository;
  private final VotesRepository votesRepository;
  private final UsersRepository usersRepository;
  private final TagsRepository tagsRepository;
  private final TagBindingsRepository tagBindingsRepository;
  private final CommentsRepository commentsRepository;


  public PostsServiceImpl(
      @Qualifier("PostsRepository") PostsRepository postsRepository,
      @Qualifier("VotesRepository") VotesRepository votesRepository,
      @Qualifier("UsersRepository") UsersRepository usersRepository,
      @Qualifier("TagsRepository") TagsRepository tagsRepository,
      @Qualifier("TagBindingsRepository") TagBindingsRepository tagBindingsRepository,
      @Qualifier("CommentsRepository") CommentsRepository commentsRepository) {
    this.votesRepository = votesRepository;
    this.postsRepository = postsRepository;
    this.usersRepository = usersRepository;
    this.tagsRepository = tagsRepository;
    this.tagBindingsRepository = tagBindingsRepository;
    this.commentsRepository = commentsRepository;
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
        Integer.parseInt(dateParams[YEAR_INDEX]),
        Integer.parseInt(dateParams[MONTH_INDEX]),
        Integer.parseInt(dateParams[DAY_INDEX]),
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
    postResponse.setTimestamp(post.getTime().getTime() / TIME_DIVIDER);
    postResponse.setActive(post.getIsActive() == ACTIVE_POST_MARKER);

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
      comment.setTimestamp(pComment.getTime().getTime() / TIME_DIVIDER);
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
      long timestamp, short active, String title, List<String> tagNames,
      String text, Principal principal, boolean premoderationMode) {

    Post post = new Post();
    User user = usersRepository.findFirstByEmail(principal.getName());

    post.setUser(user);

    PostEditResponse response = validatePostData(title, text);

    if (!response.isResult()) {
      return response;
    }

    fillPost(post, active, timestamp, title, text, premoderationMode);
    postsRepository.saveAndFlush(post);
    addTags(post, tagNames);

    return response;
  }

  @Override
  public synchronized PostEditResponse editPost(
      int id, long timestamp, short active, String title,
      List<String> tagNames, String text, boolean premoderationMode) {

    Post post = postsRepository.findPostById(id);

    if (userHasNotRightToEdit(post)) {
      return new PostEditResponse();
    }

    PostEditResponse response = validatePostData(title, text);

    if (!response.isResult()) {
      return response;
    }

    fillPost(post, active, timestamp, title, text, premoderationMode);
    clearPostSubData(post);
    postsRepository.saveAndFlush(post);
    updatePostTags(post, tagNames);

    return response;
  }

  @Override
  public synchronized ResponseEntity<?> addComment(
      int parentId, int postId, String text, Principal principal) {

    Post post = postsRepository.findPostById(postId);

    if ((post == null) || (!post.getModerationStatus().equals(ModerationStatusType.ACCEPTED)) ||
        (parentId != DEFAULT_VALUE &&
            post.getComments().stream().noneMatch(comment -> comment.getId() == parentId))) {

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    PostEditResponse creationResponse = new PostEditResponse();
    PostErrors creationErrors = getCommentErrors(text, creationResponse);

    if (!creationResponse.isResult()) {
      creationResponse.setCreationErrors(creationErrors);
      return new ResponseEntity<>(creationResponse, HttpStatus.BAD_REQUEST);
    }

    PostComment comment = new PostComment();
    User user = usersRepository.findFirstByEmail(principal.getName());

    comment.setUser(user);
    comment.setPost(post);

    if (parentId != DEFAULT_VALUE) {
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
  public StatisticsResponse getAllStatistics(boolean statisticMode) {

    if (statisticMode || isModerator()) {
      List<Post> posts = postsRepository.findAll();
      return getStatistics(posts);
    }

    return new StatisticsResponse();
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


  private PostEditResponse validatePostData(String title, String text) {
    PostEditResponse editResponse = new PostEditResponse();
    PostErrors creationErrors = getPostErrors(title, text, editResponse);

    if (!editResponse.isResult()) {
      editResponse.setCreationErrors(creationErrors);
    }

    return editResponse;
  }

  private void fillPost(
      Post post, short active, long timestamp,
      String title, String text, boolean premoderationMode) {

    post.setIsActive(active);

    long currentTime = System.currentTimeMillis();

    post.setTime(timestamp < currentTime / TIME_DIVIDER
        ? new Timestamp(currentTime)
        : new Timestamp(timestamp * TIME_DIVIDER));

    post.setTitle(title);

    text = uploadingImages(text, post);

    post.setText(text);

    updateModerationStatus(post, premoderationMode);
  }

  private void clearPostSubData(Post post) {
    votesRepository.deleteAll(votesRepository.findAllByPost(post));
    votesRepository.flush();

    post.setComments(new ArrayList<>());
    post.setViewCount(DEFAULT_VALUE);
  }

  private StatisticsResponse getStatistics(List<Post> posts) {
    StatisticsResponse response = new StatisticsResponse();
    int likesCount = DEFAULT_VALUE, dislikesCount = DEFAULT_VALUE, viewsCount = DEFAULT_VALUE;
    long firstPublication = System.currentTimeMillis() / TIME_DIVIDER;

    for (Post post : posts) {
      likesCount += getLikesCountByPostId(post.getId());
      dislikesCount += getDislikeCountByPostId(post.getId());
      viewsCount += post.getViewCount();
      long publicationTime = post.getTime().getTime() / TIME_DIVIDER;

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

  private void updateModerationStatus(Post post, boolean premoderationMode) {

    if ((!premoderationMode || isModerator()) && post.getIsActive() == ACTIVE_POST_MARKER) {
      post.setModerationStatus(ModerationStatusType.ACCEPTED);
    } else {
      post.setModerationStatus(ModerationStatusType.NEW);
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
      path = path.substring(0, path.lastIndexOf("images/") + IMAGE_PATH_OFFSET);
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

      path = path.substring(FILENAME_START_INDEX, path.length() - FILENAME_END_INDEX_OFFSET);
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
    postPreview.setTimestamp(post.getTime().getTime() / TIME_DIVIDER);
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
    int prepostMaxLength = PREPOST_STRING_COUNT * PREPOST_STRING_LENGTH;

    if (text.length() > prepostMaxLength) {
      for (int i = 0; i < PREPOST_STRING_COUNT; ) {
        announce.append(text, i + PREPOST_STRING_LENGTH, ++i * PREPOST_STRING_LENGTH);
      }

      announce.append("...");
    } else if (text.length() > prepostMaxLength - PREPOST_STRING_LENGTH) {
      for (int i = 0; i < PREPOST_STRING_COUNT; ) {
        announce.append(text, i + PREPOST_STRING_LENGTH, ++i * PREPOST_STRING_LENGTH);
      }
    } else if (text.length() > PREPOST_STRING_LENGTH) {
      for (int i = 0; i < PREPOST_STRING_COUNT - 1; ) {
        announce.append(text, i + PREPOST_STRING_LENGTH, ++i * PREPOST_STRING_LENGTH);
      }
    } else {
      return text;
    }

    return announce.toString();
  }

  private int getLikesCountByPostId(int postId) {
    AtomicInteger count = new AtomicInteger();

    votesRepository.findAll().forEach(vote -> {
      if (vote.getPost().getId() == postId && vote.getValue() == LIKE_VALUE) {
        count.getAndIncrement();
      }
    });

    return count.get();
  }

  private int getDislikeCountByPostId(int postId) {
    AtomicInteger count = new AtomicInteger();

    votesRepository.findAll().forEach(vote -> {
      if (vote.getPost().getId() == postId && vote.getValue() == DISLIKE_VALUE) {
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

  private PostErrors getPostErrors(String title, String text, PostEditResponse response) {
    PostErrors errors = new PostErrors();

    if (title.length() < MIN_TITLE_SIZE) {
      errors.setTitle("Заголовок не установлен");
      response.setResult(false);
    }

    if (text.length() < MIN_TEXT_SIZE) {
      errors.setText("Текст публикации слишком короткий");
      response.setResult(false);
    }

    return errors;
  }

  private PostErrors getCommentErrors(String text, PostEditResponse response) {
    PostErrors errors = new PostErrors();

    if (text.length() < MIN_COMMENT_SIZE) {
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

  private void addTags(Post post, List<String> tagNames) {
    List<Tag> blogTags = tagsRepository.findAll();
    List<Tag> newTags = new ArrayList<>();
    List<TagBinding> newTagBindings = new ArrayList<>();
    List<String> blogTagNames = blogTags.stream()
        .map(Tag::getName).collect(Collectors.toList());

    tagNames.forEach(t -> t = t.toLowerCase());

    for (String tagName : tagNames) {
      Tag tag = blogTags.stream().filter(t -> t.getName().equals(tagName))
          .findFirst().orElse(new Tag());

      if (!blogTagNames.contains(tagName)) {
        tag.setName(tagName.toLowerCase());
        newTags.add(tag);
      }

      newTagBindings.add(getTagBinding(tag, post));
    }

    tagsRepository.saveAll(newTags);
    tagBindingsRepository.saveAll(newTagBindings);

    tagsRepository.flush();
    tagBindingsRepository.flush();
  }

  private void updatePostTags(Post post, List<String> tagNames) {
    List<String> currentTagNames =
        post.getTags().stream().map(Tag::getName).collect(Collectors.toList());
    List<Tag> tagsForCheck = new ArrayList<>();

    List<Tag> blogTags = tagsRepository.findAll();
    List<TagBinding> blogTagBindings = tagBindingsRepository.findAll();

    for (String currentTagName : currentTagNames) {
      boolean mustDeleted = true;

      for (String tagName : tagNames) {
        if (tagName.equalsIgnoreCase(currentTagName)) {
          mustDeleted = false;
          break;
        }
      }

      if (mustDeleted) {
        Tag tag = blogTags.stream().filter(t -> t.getName().equals(currentTagName))
            .findFirst().orElse(null);
        blogTagBindings.stream()
            .filter(bind -> bind.getTag().equals(tag) && bind.getPost().equals(post))
            .findFirst().ifPresent(tagBindingsRepository::delete);

        tagsForCheck.add(tag);
      }
    }

    for (String tagName : tagNames) {
      Tag tag = blogTags.stream().filter(t -> t.getName().equals(tagName.toLowerCase()))
          .findFirst().orElse(null);

      if (tag == null) {
        Tag newTag = new Tag();

        newTag.setName(tagName.toLowerCase());
        tagsRepository.save(newTag);
        tagBindingsRepository.save(getTagBinding(newTag, post));
      } else {
        TagBinding tagBinding = blogTagBindings.stream()
            .filter(bind -> bind.getTag().equals(tag) && bind.getPost().equals(post))
            .findFirst().orElse(null);

        if (tagBinding == null) {
          tagBindingsRepository.save(getTagBinding(tag, post));
        }
      }
    }

    tagBindingsRepository.flush();
    blogTagBindings = tagBindingsRepository.findAll();

    for (Tag tag : tagsForCheck) {
      TagBinding tagBinding = blogTagBindings.stream()
          .filter(bind -> bind.getTag().equals(tag))
          .findFirst().orElse(null);

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