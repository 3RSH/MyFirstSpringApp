package main.repository.comments;

import java.util.List;
import main.model.Post;
import main.model.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service("CommentsRepository")
@Repository
public interface CommentsRepository extends JpaRepository<PostComment, Integer> {

  List<PostComment> findAllByPost(Post post);
}