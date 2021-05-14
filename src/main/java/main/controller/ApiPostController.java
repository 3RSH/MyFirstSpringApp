package main.controller;

import main.api.response.PostPreviewResponse;
import main.service.posts.PostsServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
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
  private PostPreviewResponse posts(
      @RequestParam(required = false, defaultValue = "0") int offset,
      @RequestParam(required = false, defaultValue = "10") int limit,
      @RequestParam(required = false, defaultValue = "recent") String mode) {

    return postsService.getPostsPreview(offset, limit, mode);
  }

  @GetMapping("/search")
  private PostPreviewResponse postsByQuery(
      @RequestParam int offset,
      @RequestParam int limit,
      @RequestParam String query) {

    return postsService.getPostsPreviewByQuery(offset, limit, query);
  }

  @GetMapping("/byDate")
  private PostPreviewResponse postsByDate(
      @RequestParam int offset,
      @RequestParam int limit,
      @RequestParam String date) {

    return postsService.getPostsPreviewByDate(offset, limit, date);
  }
}