package main.repository.tags;

import java.util.List;
import main.model.Tag;

public interface TagsRepository {

  Tag getTag(int tagId);

  List<Tag> getAllTags();
}