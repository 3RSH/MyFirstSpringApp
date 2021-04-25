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
  private PostPreviewResponse posts(@RequestParam int offset, @RequestParam int limit,
      @RequestParam String mode) {
    return postsService.getPostsPreview(offset, limit, mode);
  }
}