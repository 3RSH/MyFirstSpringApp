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


}
