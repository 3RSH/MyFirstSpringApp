package main.service.posts;

import java.security.Principal;
import main.api.response.PostPreviewResponse;
import main.api.response.PostResponse;

public interface PostsService {

  PostPreviewResponse getPostsPreview(int offset, int limit, String mode);

  PostPreviewResponse getPostsPreviewByQuery(int offset, int limit, String query);

  PostPreviewResponse getPostsPreviewByDate(int offset, int limit, String date);

  PostPreviewResponse getPostsPreviewByTag(int offset, int limit, String tag);

  PostResponse getPostById(int id);

  PostPreviewResponse getMyPostsPreview(
      int offset, int limit,
      String mode,
      Principal principal);
}