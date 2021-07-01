package main.repository.comments;

import main.model.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service("CommentsRepository")
@Repository
public interface CommentsRepository extends JpaRepository<PostComment, Integer> {

}