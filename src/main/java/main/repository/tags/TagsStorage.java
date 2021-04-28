package main.repository.tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import main.model.Tag;
import org.springframework.stereotype.Service;

@Service("TagsStorage")
public class TagsStorage implements TagsRepository {

  private final PostgresTagsRepository sqlTagsRepository;

  public TagsStorage(PostgresTagsRepository sqlTagsRepository) {
    this.sqlTagsRepository = sqlTagsRepository;
  }

  @Override
  public Tag getTag(int tagId) {
    Optional<Tag> tag = sqlTagsRepository.findById(tagId);
    return tag.orElse(null);
  }

  @Override
  public List<Tag> getAllTags() {
    List<Tag> tags = new ArrayList<>();
    sqlTagsRepository.findAll().forEach(tags::add);
    return tags;
  }
}
