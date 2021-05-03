package main.repository.posts;

import main.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service("PostsRepository")
@Repository
public interface PostsRepository extends JpaRepository<Post, Integer> {

}