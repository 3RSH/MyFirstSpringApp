package main.repository.tags;

import main.model.Tag;
import org.springframework.data.repository.CrudRepository;

public interface PostgresTagsRepository extends CrudRepository<Tag, Integer> {

}
