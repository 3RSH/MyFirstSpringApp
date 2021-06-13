package main.controller;

import java.security.Principal;
import main.api.response.PostPreviewResponse;
import main.api.response.PostResponse;
import main.service.posts.PostsServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/post")
public class ApiPostController {

  private final PostsServiceImpl postsService;


  public ApiPostController(PostsServiceImpl postsService) {
    this.postsService = postsService;
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
  public ResponseEntity getPostById(@PathVariable("ID") int id) {
    PostResponse postResponse = postsService.getPostById(id);

    return postResponse.getId() != 0
        ? new ResponseEntity(postResponse, HttpStatus.OK)
        : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
  }

  @GetMapping("/my")
  @PreAuthorize("hasAuthority('user:write')")
  public PostPreviewResponse myPosts(
      @RequestParam(required = false, defaultValue = "0") int offset,
      @RequestParam(required = false, defaultValue = "10") int limit,
      @RequestParam(required = false, defaultValue = "inactive") String status,
      Principal principal) {

    return postsService.getMyPostsPreview(offset, limit, status, principal);
  }
}