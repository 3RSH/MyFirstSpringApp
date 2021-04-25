package main.service.posts;

import main.api.response.PostPreviewResponse;

public interface PostsService {

  PostPreviewResponse getPostsPreview(int offset, int limit, String mode);
}