package main.repository.posts;

import java.util.List;
import main.api.PostPreview;
import main.model.Post;

public interface PostsRepository {

  Post getPost(int postId);
  List<Post> getAllPosts();
  long count();
}