package main.service.posts;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import main.api.Comment;
import main.api.CommentUserPreview;
import main.api.PostErrors;
import main.api.PostPreview;
import main.api.UserPreview;
import main.api.request.AddCommentRequest;
import main.api.request.AddPostRequest;
import main.api.request.AddVoteRequest;
import main.api.request.ModerateRequest;
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

  private static final int YEAR_INDEX = 0;
  private static final int MONTH_INDEX = 1;
  private static final int DAY_INDEX = 2;
  private static final int TIME_DIVIDER = 1000;
  private static final short ACTIVE_POST_MARKER = 1;
  private static final int DEFAULT_VALUE = 0;
  private static final int FILENAME_START_INDEX = 10;
  private static final int FILENAME_END_INDEX_OFFSET = 2;
  private static final int PREPOST_STRING_LENGTH = 50;
  private static final int PREPOST_STRING_COUNT = 3;
  private static final int LIKE_VALUE = 1;
  private static final int DISLIKE_VALUE = -1;
  private static final int MIN_TITLE_SIZE = 3;
  private static final int MIN_TEXT_SIZE = 50;
  private static final int MIN_COMMENT_SIZE = 3;

  private static final String RECENT_POST_MODE = "recent";
  private static final String POPULAR_POST_MODE = "popular";
  private static final String BEST_POST_MODE = "best";
  private static final String EARLY_POST_MODE = "early";
  private static final String PENDING_POST_MODE = "pending";
  private static final String DECLINED_POST_MODE = "declined";
  private static final String PUBLISHED_POST_MODE = "published";
  private static final String ACCEPTED_POST_MODE = "accepted";
  private static final String ACCEPT_DECISION_VALUE = "accept";
  private static final String EMPTY_STRING = "";
  private static final String TITLE_ERROR = "Заголовок не установлен";
  private static final String POST_TEXT_ERROR = "Текст публикации слишком короткий";
  private static final String COMMENT_ERROR = "Текст комментария не задан или слишком короткий";
  private static final String CLOUDINARY_HOME = "devBlog";
  private static final String TEMP_FOLDER = "devBlog/temp/";
  private static final String IMAGES_FOLDER = "devBlog/postsImages/";
  private static final String UPLOADER_OVERWRITE_PROP = "overwrite";
  private static final String UPLOADER_INVALIDATE_PROP = "invalidate";
  private static final String TRUE_VALUE = "true";


  private static final String EMPTY_QUERY_REGEX = "\\s*";
  private static final String DATE_SEPARATOR_REGEX = "-";
  private static final String SLASH_REGEX = "/";
  private static final String BACKSLASH_REGEX = "\\\\";
  private static final String IMAGE_TAG_REGEX = "<img src=\"[^>]+devBlog/postsImages/[^>]+\">";
  private static final String TEMP_IMAGE_TAG_REGEX = "<img src=\"[^>]+devBlog/temp/[^>]+\">";
  private static final String CLASSIC_HTML_TAG_REGEX = "<.*?>";

  private static final String SYMBOL_HTML_TAG_REGEX = "&[a-zA-Z]{1,10};";

  private static final Cloudinary imageCloud = new Cloudinary();

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
      case (POPULAR_POST_MODE):
        return getPostPreviewResponse(postsRepository.findPopularPosts(page));

      case (BEST_POST_MODE):
        return getPostPreviewResponse(postsRepository.findBestPosts(page));

      case (EARLY_POST_MODE):
        return getPostPreviewResponse(postsRepository.findEarlyPosts(page));

      default:
        return getPostPreviewResponse(postsRepository.findRecentPosts(page));
    }
  }

  @Override
  public PostPreviewResponse getMyPostsPreview(int offset, int limit, String status) {
    Pageable page = PageRequest.of(offset / limit, limit);
    int userId = usersRepository
        .findFirstByEmail(
            SecurityContextHolder.getContext().getAuthentication().getName())
        .getId();

    switch (status) {
      case (PENDING_POST_MODE):
        return getPostPreviewResponse(postsRepository.findPendingPostsByUser(userId, page));

      case (DECLINED_POST_MODE):
        return getPostPreviewResponse(postsRepository.findDeclinedPostsByUser(userId, page));

      case (PUBLISHED_POST_MODE):
        return getPostPreviewResponse(postsRepository.findPublishedPostsByUser(userId, page));

      default:
        return getPostPreviewResponse(postsRepository.findInactivePostsByUser(userId, page));
    }
  }

  @Override
  public PostPreviewResponse getPostsPreviewByQuery(int offset, int limit, String query) {

    if (query.matches(EMPTY_QUERY_REGEX)) {
      return getPostsPreview(offset, limit, RECENT_POST_MODE);
    }

    Pageable page = PageRequest.of(offset / limit, limit);

    return getPostPreviewResponse(postsRepository.findAllByTitleContainingIgnoreCase(query, page));
  }

  @Override
  public PostPreviewResponse getPostsPreviewByDate(int offset, int limit, String date) {
    Pageable page = PageRequest.of(offset / limit, limit);
    String[] dateParams = date.split(DATE_SEPARATOR_REGEX);

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
      AddPostRequest request, Principal principal, boolean premoderationMode) {

    Post post = new Post();
    User user = usersRepository.findFirstByEmail(principal.getName());

    post.setUser(user);

    PostEditResponse response = validatePostData(request.getTitle(), request.getText());

    if (!response.isResult()) {
      return response;
    }

    try {
      fillPost(
          post,
          request.getActive(),
          request.getTimestamp(),
          request.getTitle(),
          request.getText(),
          premoderationMode);

    } catch (IOException e) {
      e.printStackTrace();
    }

    postsRepository.saveAndFlush(post);
    addTags(post, request.getTagNames());

    return response;
  }

  @Override
  public synchronized PostEditResponse editPost(
      AddPostRequest request, int id, boolean premoderationMode) {

    Post post = postsRepository.findPostById(id);

    if (userHasNotRightToEdit(post)) {
      return new PostEditResponse();
    }

    PostEditResponse response = validatePostData(request.getTitle(), request.getText());

    if (!response.isResult()) {
      return response;
    }

    try {
      fillPost(
          post,
          request.getActive(),
          request.getTimestamp(),
          request.getTitle(),
          request.getText(),
          premoderationMode);

    } catch (IOException e) {
      e.printStackTrace();
    }

    clearPostSubData(post);
    postsRepository.saveAndFlush(post);
    updatePostTags(post, request.getTagNames());

    return response;
  }

  @Override
  public synchronized PostEditResponse addComment(
      AddCommentRequest request, Principal principal) {

    PostEditResponse response = new PostEditResponse();
    Post post = postsRepository.findPostById(request.getPostId());

    if ((post == null) ||
        (!post.getModerationStatus().equals(ModerationStatusType.ACCEPTED)) ||
        (request.getParentId() != DEFAULT_VALUE &&
            post.getComments().stream().noneMatch(
                comment -> comment.getId() == request.getParentId()))) {

      deleteFiles(getPathsFromText(request.getText(), TEMP_IMAGE_TAG_REGEX));
      return response;
    }

    PostErrors creationErrors = getCommentErrors(request.getText(), response);

    if (!response.isResult()) {
      response.setCreationErrors(creationErrors);
      return response;
    }

    PostComment comment = new PostComment();
    User user = usersRepository.findFirstByEmail(principal.getName());

    comment.setUser(user);
    comment.setPost(post);

    if (request.getParentId() != DEFAULT_VALUE) {
      comment.setParentComment(commentsRepository.getOne(request.getParentId()));
    }

    long currentTime = System.currentTimeMillis();

    comment.setTime(new Timestamp(currentTime));

    try {
      String text = saveNewFiles(post, request.getText());
      comment.setText(text);

    } catch (IOException e) {
      e.printStackTrace();
    }

    commentsRepository.saveAndFlush(comment);

    CommentResponse commentResponse = new CommentResponse();

    commentResponse.setId(comment.getId());

    response.setCommentResponse(commentResponse);

    return response;
  }

  @Override
  public synchronized VoteResponse addVote(AddVoteRequest request, short voteValue) {
    VoteResponse response = new VoteResponse();
    User user = getCurrentUser();
    Post post = postsRepository.findPostById(request.getPostId());

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
      case (DECLINED_POST_MODE):
        return getPostPreviewResponse(
            postsRepository.findDeclinedPostsByModerator(moderatorId, page));

      case (ACCEPTED_POST_MODE):
        return getPostPreviewResponse(
            postsRepository.findAcceptedPosts(page));

      default:
        return getPostPreviewResponse(postsRepository.findNewModeratedPosts(page));
    }
  }

  @Override
  public synchronized PostEditResponse moderatePost(ModerateRequest request) {
    PostEditResponse response = new PostEditResponse();
    Post post = postsRepository.findPostById(request.getPostId());

    if (post == null) {
      response.setResult(false);
      return response;
    }

    if (request.getDecision().equals(ACCEPT_DECISION_VALUE)) {
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
      String title, String text, boolean premoderationMode) throws IOException {

    post.setIsActive(active);

    long currentTime = System.currentTimeMillis();

    post.setTime(timestamp < currentTime / TIME_DIVIDER
        ? new Timestamp(currentTime)
        : new Timestamp(timestamp * TIME_DIVIDER));

    post.setTitle(title);

    if (post.getText() == null) {
      post.setText(EMPTY_STRING);
    }

    text = saveNewFiles(post, text);

    updateFiles(post, getPathsFromText(text, IMAGE_TAG_REGEX));

    post.setText(text);

    if (isModerator()) {
      post.setModerator(getCurrentUser());
    }

    updateModerationStatus(post, premoderationMode);
  }

  private void clearPostSubData(Post post) {
    votesRepository.deleteAll(votesRepository.findAllByPost(post));
    votesRepository.flush();

    List<PostComment> comments = commentsRepository.findAllByPost(post);

    List<String> commentsImages = new ArrayList<>();

    for (PostComment comment : comments) {
      commentsImages.addAll(getPathsFromText(comment.getText(), IMAGE_TAG_REGEX));
    }

    deleteFiles(commentsImages);

    commentsRepository.deleteAll(comments);
    commentsRepository.flush();

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

  private void updateFiles(Post post, List<String> paths) {
    String originalText = post.getText();
    List<String> oldPaths = getPathsFromText(originalText, IMAGE_TAG_REGEX);

    for (String path : paths) {
      oldPaths.remove(path);
    }

    deleteFiles(oldPaths);
  }

  private void deleteFiles(List<String> source) {
    convertPathsForFile(source);

    List<String> resources = new ArrayList<>();

    for (String path : source) {
      resources.add(path.substring(path.indexOf(CLOUDINARY_HOME), path.lastIndexOf(".")));
    }

    try {
      imageCloud.api().deleteResources(resources, ObjectUtils.emptyMap());

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String saveNewFiles(Post post, String text) throws IOException {
    List<String> tempPaths = getPathsFromText(text, TEMP_IMAGE_TAG_REGEX);
    List<String> newPaths = new ArrayList<>(tempPaths.size());

    convertPathsForFile(tempPaths);

    SecurityContext currentContext = SecurityContextHolder.getContext();
    User user = usersRepository.findFirstByEmail(
        currentContext.getAuthentication().getName());

    for (String path : tempPaths) {

      String newPath = path.replaceFirst(
          TEMP_FOLDER + user.getId() + SLASH_REGEX,
          IMAGES_FOLDER + post.getId() + SLASH_REGEX);

      newPaths.add(newPath);

      String fromId = path.substring(
          path.indexOf(CLOUDINARY_HOME), path.lastIndexOf("."));

      String toId = newPath.substring(
          newPath.indexOf(CLOUDINARY_HOME), newPath.lastIndexOf("."));

      imageCloud.uploader().rename(fromId, toId, ObjectUtils.asMap(
          UPLOADER_OVERWRITE_PROP, TRUE_VALUE,
          UPLOADER_INVALIDATE_PROP, TRUE_VALUE));
    }

    try {
      imageCloud.api().deleteResourcesByPrefix(
          TEMP_FOLDER + user.getId(), ObjectUtils.emptyMap());

      imageCloud.api().deleteFolder(
          TEMP_FOLDER + user.getId(), ObjectUtils.emptyMap());

    } catch (Exception e) {
      e.printStackTrace();
    }

    for (int i = 0; i < tempPaths.size(); i++) {
      text = text.replaceFirst(tempPaths.get(i), newPaths.get(i));
    }

    return text;
  }

  private void convertPathsForFile(List<String> paths) {
    for (int i = 0; i < paths.size(); i++) {
      String path = paths.get(i);

      path = path.substring(FILENAME_START_INDEX, path.length() - FILENAME_END_INDEX_OFFSET);
      path = path.replaceAll(BACKSLASH_REGEX, SLASH_REGEX);

      paths.set(i, path);
    }
  }

  private List<String> getPathsFromText(String text, String tagRegex) {
    List<String> paths = new ArrayList<>();
    Pattern pattern = Pattern.compile(tagRegex);
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
    String text = post.getText().replaceAll(CLASSIC_HTML_TAG_REGEX, EMPTY_STRING)
        .replaceAll(SYMBOL_HTML_TAG_REGEX, EMPTY_STRING);
    int prepostMaxLength = PREPOST_STRING_COUNT * PREPOST_STRING_LENGTH;

    if (text.length() > prepostMaxLength) {
      announce.append(text, 0, PREPOST_STRING_LENGTH).append("\n")
          .append(text, PREPOST_STRING_LENGTH, 2 * PREPOST_STRING_LENGTH).append("\n")
          .append(text, 2 * PREPOST_STRING_LENGTH, 3 * PREPOST_STRING_LENGTH)
          .append("...");

    } else if (text.length() > prepostMaxLength - PREPOST_STRING_LENGTH) {
      announce.append(text, 0, PREPOST_STRING_LENGTH).append("\n")
          .append(text, PREPOST_STRING_LENGTH, 2 * PREPOST_STRING_LENGTH).append("\n")
          .append(text.substring(2 * PREPOST_STRING_LENGTH));

    } else if (text.length() > prepostMaxLength - 2 * PREPOST_STRING_LENGTH) {
      announce.append(text, 0, PREPOST_STRING_LENGTH).append("\n")
          .append(text.substring(PREPOST_STRING_LENGTH));

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
      errors.setTitle(TITLE_ERROR);
      response.setResult(false);
    }

    if (text.length() < MIN_TEXT_SIZE) {
      errors.setText(POST_TEXT_ERROR);
      response.setResult(false);
    }

    return errors;
  }

  private PostErrors getCommentErrors(String text, PostEditResponse response) {
    PostErrors errors = new PostErrors();

    if (text.length() < MIN_COMMENT_SIZE) {
      errors.setText(COMMENT_ERROR);
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