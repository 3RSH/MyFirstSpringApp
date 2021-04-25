package main.repository.posts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import main.api.PostPreview;
import main.api.UserPreview;
import main.model.Post;
import main.model.Post.ModerationStatusType;
import org.apache.tomcat.jni.Time;
import org.springframework.stereotype.Service;

@Service("PostsStorage")
public class PostsStorage implements PostsRepository {

  private final PostgresPostsRepository sqlPostsRepository;

  public PostsStorage(PostgresPostsRepository sqlPostsRepository) {
    this.sqlPostsRepository = sqlPostsRepository;
  }

  @Override
  public Post getPost(int postId) {
    Optional<Post> post = sqlPostsRepository.findById(postId);
    return post.orElse(null);
  }

  @Override
  public List<Post> getAllPosts() {
    List<Post> posts = new ArrayList<>();
    sqlPostsRepository.findAll().forEach(posts::add);
    return posts;
  }

  @Override
  public long count() {
    return sqlPostsRepository.count();
  }

//  @Override
//  public List<PostPreview> getPostsPreview() {
//    List<PostPreview> posts = new ArrayList<>();
//
//    PostPreview postPreview= new PostPreview();
//    UserPreview userPreview = new UserPreview();
//
//    sqlPostsRepository.findAll().forEach(post -> {
//      if (post.getIsActive() == 1
//          && post.getModerationStatus() == ModerationStatusType.ACCEPTED) {
//        if (post.getTime().getTime() <= System.currentTimeMillis()) {
//          postPreview.setId(post.getId());
//          postPreview.setTimestamp(post.getTime().getTime());
//
//          userPreview.setId(post.getUser().getId());
//          userPreview.setName(post.getUser().getName());
//          postPreview.setUser(userPreview);
//
//          postPreview.setTitle(post.getTitle());
//          postPreview.setAnnounce(post.getText().length() > 150
//              ? (post.getText().substring(0, 150) + "...")
//              : post.getText());
//
//
//          postPreview.setId(post.getId());
//          postPreview.setId(post.getId());
//          postPreview.setId(post.getId());
//          postPreview.setId(post.getId());
//          postPreview.setId(post.getId());
//          postPreview.setId(post.getId());
//
//
//
//
//
//          posts.add(postPreview);
//        }
//      }
//    });
//    return posts;
//  }


}
