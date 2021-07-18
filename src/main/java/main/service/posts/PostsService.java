package main.service.posts;

import java.security.Principal;
import java.util.List;
import main.api.response.PostEditResponse;
import main.api.response.PostPreviewResponse;
import main.api.response.PostResponse;
import main.api.response.StatisticsResponse;
import main.api.response.VoteResponse;
import org.springframework.http.ResponseEntity;

public interface PostsService {

  PostPreviewResponse getPostsPreview(int offset, int limit, String mode);

  PostPreviewResponse getPostsPreviewByQuery(int offset, int limit, String query);

  PostPreviewResponse getPostsPreviewByDate(int offset, int limit, String date);

  PostPreviewResponse getPostsPreviewByTag(int offset, int limit, String tag);

  PostResponse getPostById(int id);

  PostPreviewResponse getMyPostsPreview(int offset, int limit, String mode);

  PostEditResponse addPost(
      long timestamp, short active, String title,
      List<String> tags, String text, Principal principal);

  PostEditResponse editPost(
      int id, long timestamp, short active, String title, List<String> tags, String text);

  ResponseEntity<?> addComment(int parentId, int postId, String text, Principal principal);

  VoteResponse addVote(int postId, short voteValue);

  StatisticsResponse getMyStatistics();

  ResponseEntity<?> getAllStatistics();

  PostPreviewResponse getModeratedPostsPreview(int offset, int limit, String mode);
}