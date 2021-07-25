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
  public ResponseEntity<PostPreviewResponse> posts(
      @RequestParam(required = false, defaultValue = "0") int offset,
      @RequestParam(required = false, defaultValue = "10") int limit,
      @RequestParam(required = false, defaultValue = "recent") String mode) {

    return new ResponseEntity<>(
        postsService.getPostsPreview(offset, limit, mode), HttpStatus.OK);
  }

  @GetMapping("/search")
  public ResponseEntity<PostPreviewResponse> postsByQuery(
      @RequestParam(required = false, defaultValue = "0") int offset,
      @RequestParam(required = false, defaultValue = "10") int limit,
      @RequestParam String query) {

    return new ResponseEntity<>(
        postsService.getPostsPreviewByQuery(offset, limit, query), HttpStatus.OK);
  }

  @GetMapping("/byDate")
  public ResponseEntity<PostPreviewResponse> postsByDate(
      @RequestParam(required = false, defaultValue = "0") int offset,
      @RequestParam(required = false, defaultValue = "10") int limit,
      @RequestParam String date) {

    return new ResponseEntity<>(
        postsService.getPostsPreviewByDate(offset, limit, date), HttpStatus.OK);
  }

  @GetMapping("/byTag")
  public ResponseEntity<PostPreviewResponse> postsByTag(
      @RequestParam(required = false, defaultValue = "0") int offset,
      @RequestParam(required = false, defaultValue = "10") int limit,
      @RequestParam String tag) {

    return new ResponseEntity<>(
        postsService.getPostsPreviewByTag(offset, limit, tag), HttpStatus.OK);
  }

  @GetMapping("/{ID}")
  public ResponseEntity<?> getPostById(@PathVariable("ID") int id) {
    PostResponse postResponse = postsService.getPostById(id);

    return postResponse.getId() != NULL_ID
        ? new ResponseEntity<>(postResponse, HttpStatus.OK)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @GetMapping("/my")
  @PreAuthorize("hasAuthority('use')")
  public ResponseEntity<PostPreviewResponse> myPosts(
      @RequestParam(required = false, defaultValue = "0") int offset,
      @RequestParam(required = false, defaultValue = "10") int limit,
      @RequestParam(required = false, defaultValue = "inactive") String status) {

    return new ResponseEntity<>(
        postsService.getMyPostsPreview(offset, limit, status), HttpStatus.OK);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('use')")
  public ResponseEntity<PostEditResponse> addPost(
      @RequestBody AddPostRequest request, Principal principal) {

    return new ResponseEntity<>(
        postsService.addPost(request, principal, settingsService.getSetting("POST_PREMODERATION")),
        HttpStatus.OK);
  }

  @PutMapping(path = "/{ID}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('use')")
  public ResponseEntity<PostEditResponse> editPost(
      @PathVariable("ID") int id,
      @RequestBody AddPostRequest request) {

    return new ResponseEntity<>(
        postsService.editPost(request, id, settingsService.getSetting("POST_PREMODERATION")),
        HttpStatus.OK);
  }

  @PostMapping(path = "/like", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('use')")
  public ResponseEntity<VoteResponse> addLike(@RequestBody AddVoteRequest request) {
    return new ResponseEntity<>(
        postsService.addVote(request, LIKE_VOTE), HttpStatus.OK);
  }

  @PostMapping(path = "/dislike", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('use')")
  public ResponseEntity<VoteResponse> addDislike(@RequestBody AddVoteRequest request) {
    return new ResponseEntity<>(
        postsService.addVote(request, DISLIKE_VOTE), HttpStatus.OK);
  }

  @GetMapping("/moderation")
  @PreAuthorize("hasAuthority('moderate')")
  public ResponseEntity<PostPreviewResponse> moderatedPosts(
      @RequestParam(required = false, defaultValue = "0") int offset,
      @RequestParam(required = false, defaultValue = "10") int limit,
      @RequestParam(required = false, defaultValue = "new") String status) {

    return new ResponseEntity<>(
        postsService.getModeratedPostsPreview(offset, limit, status), HttpStatus.OK);
  }
}