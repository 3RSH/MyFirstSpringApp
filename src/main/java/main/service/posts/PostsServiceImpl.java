package main.service.posts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import main.api.PostPreview;
import main.api.SortPostPreview;
import main.api.UserPreview;
import main.api.response.PostPreviewResponse;
import main.model.Post;
import main.model.Post.ModerationStatusType;
import main.repository.posts.PostsRepository;
import main.repository.posts.PostsStorage;
import main.repository.votes.VotesRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PostsServiceImpl implements PostsService {

  private final PostsRepository postStorage;
  private final VotesRepository voteStorage;

  public PostsServiceImpl(@Qualifier("PostsStorage") PostsStorage postStorage,
      @Qualifier("VotesStorage") VotesRepository voteStorage) {
    this.voteStorage = voteStorage;
    this.postStorage = postStorage;
  }

  @Override
  public PostPreviewResponse getPostsPreview(int offset, int limit, String mode) {
    PostPreviewResponse postPreviewResponse = new PostPreviewResponse();

    List<PostPreview> posts = new ArrayList<>();

    long postCount = postStorage.count();

    for (int i = 1; i <= postCount; i++) {

      Post post = postStorage.getPost(i);

      if (post.getIsActive() == 1
          && post.getModerationStatus() == ModerationStatusType.ACCEPTED
          && post.getTime().getTime() <= System.currentTimeMillis()) {

        PostPreview postPreview = new PostPreview();
        UserPreview userPreview = new UserPreview();

        postPreview.setId(post.getId());
        postPreview.setTimestamp(post.getTime().getTime() / 1000);

        userPreview.setId(post.getUser().getId());
        userPreview.setName(post.getUser().getName());
        postPreview.setUser(userPreview);

        postPreview.setTitle(post.getTitle());
        postPreview.setAnnounce(post.getText().length() > 150
            ? (post.getText().substring(0, 50) + "\n"
              + post.getText().substring(50, 100) + "\n"
              + post.getText().substring(100, 150) + "...")
            : post.getText().length() > 100
                ? (post.getText().substring(0, 50) + "\n"
                  + post.getText().substring(50, 100) + "\n"
                  + post.getText().substring(100))
                : post.getText().length() > 50
                    ? (post.getText().substring(0, 50) + "\n"
                      + post.getText().substring(50))
                    : post.getText());

        postPreview.setLikeCount(voteStorage.getLikesCountByPostId(i));
        postPreview.setDislikeCount(voteStorage.getDislikeCountByPostId(i));
        postPreview.setCommentCount(post.getComments().size());
        postPreview.setViewCount(post.getViewCount());

        posts.add(postPreview);
      }
    }

    postPreviewResponse.setCount(posts.size());

    switch (mode) {
      case ("recent"):
        posts.sort(SortPostPreview.RECENT);
        break;

      case ("popular"):
        posts.sort(SortPostPreview.POPULAR);
        break;

      case ("best"):
        posts.sort(SortPostPreview.BEST);
        break;

      case ("early"):
        posts.sort(SortPostPreview.EARLY);
        break;
    }

    List<PostPreview> postsResponse = new ArrayList<>();

    for (int i = offset; i < (offset + limit); i++) {
      if (i == posts.size()) {
        break;
      }
      postsResponse.add(posts.get(i));
    }

    postPreviewResponse.setPosts(postsResponse);
    return postPreviewResponse;
  }
}