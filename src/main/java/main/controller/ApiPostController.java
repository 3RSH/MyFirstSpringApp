package main.controller;

import java.security.Principal;
import main.api.request.AddPostRequest;
import main.api.request.AddVoteRequest;
import main.api.response.PostEditResponse;
import main.api.response.PostPreviewResponse;
import main.api.response.PostResponse;
import main.api.response.VoteResponse;
import main.service.posts.PostsServiceImpl;
import main.service.settings.SettingsServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/post")
public class ApiPostController {

  private static final int NULL_ID = 0;
  private static final short LIKE_VOTE = 1;
  private static final short DISLIKE_VOTE = -1;

  private final PostsServiceImpl postsService;
  private final SettingsServiceImpl settingsService;


  public ApiPostController(PostsServiceImpl postsService, SettingsServiceImpl settingsService) {
    this.postsService = postsService;
    this.settingsService = settingsService;
  }


  @GetMapping
  public PostPreviewResponse posts(
      @RequestParam(required = false, defaultValue = "0") int offset,
      @RequestParam(required = false, defaultValue = "10") int limit,
      @RequestParam(required = false, defaultValue = "recent") String mode) {

    return postsService.getPostsPreview(offset, limit, mode);
  }

  @GetMapping("/search")
  public PostPreviewResponse postsByQuery(
      @RequestParam(required = false, defaultValue = "0") int offset,
      @RequestParam(required = false, defaultValue = "10") int limit,
      @RequestParam String query) {

    return postsService.getPostsPreviewByQuery(offset, limit, query);
  }

  @GetMapping("/byDate")
  public PostPreviewResponse postsByDate(
      @RequestParam(required = false, defaultValue = "0") int offset,
      @RequestParam(required = false, defaultValue = "10") int limit,
      @RequestParam String date) {

    return postsService.getPostsPreviewByDate(offset, limit, date);
  }

  @GetMapping("/byTag")
  public PostPreviewResponse postsByTag(
      @RequestParam(required = false, defaultValue = "0") int offset,
      @RequestParam(required = false, defaultValue = "10") int limit,
      @RequestParam String tag) {

    return postsService.getPostsPreviewByTag(offset, limit, tag);
  }

  @GetMapping("/{ID}")
  public ResponseEntity<PostResponse> getPostById(@PathVariable("ID") int id) {
    PostResponse postResponse = postsService.getPostById(id);

    return postResponse.getId() != NULL_ID
        ? new ResponseEntity<>(postResponse, HttpStatus.OK)
        : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
  }

  @GetMapping("/my")
  @PreAuthorize("hasAuthority('use')")
  public PostPreviewResponse myPosts(
      @RequestParam(required = false, defaultValue = "0") int offset,
      @RequestParam(required = false, defaultValue = "10") int limit,
      @RequestParam(required = false, defaultValue = "inactive") String status) {

    return postsService.getMyPostsPreview(offset, limit, status);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('use')")
  public PostEditResponse addPost(
      @RequestBody AddPostRequest addPostRequest, Principal principal) {

    return postsService.addPost(
        addPostRequest.getTimestamp(),
        addPostRequest.getActive(),
        addPostRequest.getTitle(),
        addPostRequest.getTagNames(),
        addPostRequest.getText(),
        principal, settingsService.getSetting("POST_PREMODERATION"));
  }

  @PutMapping(path = "/{ID}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('use')")
  public PostEditResponse editPost(
      @PathVariable("ID") int id,
      @RequestBody AddPostRequest editPostRequest) {

    return postsService.editPost(id,
        editPostRequest.getTimestamp(),
        editPostRequest.getActive(),
        editPostRequest.getTitle(),
        editPostRequest.getTagNames(),
        editPostRequest.getText(),
        settingsService.getSetting("POST_PREMODERATION"));
  }

  @PostMapping(path = "/like", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('use')")
  public VoteResponse addLike(@RequestBody AddVoteRequest voteRequest) {
    return postsService.addVote(voteRequest.getPostId(), LIKE_VOTE);
  }

  @PostMapping(path = "/dislike", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('use')")
  public VoteResponse addDislike(@RequestBody AddVoteRequest voteRequest) {
    return postsService.addVote(voteRequest.getPostId(), DISLIKE_VOTE);
  }

  @GetMapping("/moderation")
  @PreAuthorize("hasAuthority('moderate')")
  public PostPreviewResponse moderatedPosts(
      @RequestParam(required = false, defaultValue = "0") int offset,
      @RequestParam(required = false, defaultValue = "10") int limit,
      @RequestParam(required = false, defaultValue = "new") String status) {

    return postsService.getModeratedPostsPreview(offset, limit, status);
  }
}