package main.repository.posts;

import main.model.Post;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostgresPostsRepository extends CrudRepository<Post, Integer> {

}