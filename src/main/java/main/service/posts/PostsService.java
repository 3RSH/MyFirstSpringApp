package main.service.posts;

import java.security.Principal;
import main.api.request.AddCommentRequest;
import main.api.request.AddPostRequest;
import main.api.request.AddVoteRequest;
import main.api.request.ModerateRequest;
import main.api.response.PostEditResponse;
import main.api.response.PostPreviewResponse;
import main.api.response.PostResponse;
import main.api.response.StatisticsResponse;
import main.api.response.VoteResponse;

public interface PostsService {

  PostPreviewResponse getPostsPreview(int offset, int limit, String mode);

  PostPreviewResponse getPostsPreviewByQuery(int offset, int limit, String query);

  PostPreviewResponse getPostsPreviewByDate(int offset, int limit, String date);

  PostPreviewResponse getPostsPreviewByTag(int offset, int limit, String tag);

  PostResponse getPostById(int id);

  PostPreviewResponse getMyPostsPreview(int offset, int limit, String mode);

  PostEditResponse addPost(AddPostRequest request, Principal principal, boolean premoderationMode);

  PostEditResponse editPost(AddPostRequest request, int id, boolean premoderationMode);

  PostEditResponse addComment(AddCommentRequest request, Principal principal);

  VoteResponse addVote(AddVoteRequest request, short voteValue);

  StatisticsResponse getMyStatistics();

  StatisticsResponse getAllStatistics(boolean statisticMode);

  PostPreviewResponse getModeratedPostsPreview(int offset, int limit, String mode);

  PostEditResponse moderatePost(ModerateRequest request);
}