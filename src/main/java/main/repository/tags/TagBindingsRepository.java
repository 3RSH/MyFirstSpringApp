package main.repository.tags;

import main.model.Post;
import main.model.Tag;
import main.model.TagBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service("TagBindingsRepository")
@Repository
public interface TagBindingsRepository extends JpaRepository<TagBinding, Integer> {

}