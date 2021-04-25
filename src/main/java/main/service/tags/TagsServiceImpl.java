package main.service.tags;

import java.util.ArrayList;
import java.util.List;
import main.api.TagInfo;
import main.api.response.TagResponse;
import org.springframework.stereotype.Service;

@Service
public class TagsServiceImpl implements TagsService {

  @Override
  public TagResponse getTags() {
    TagResponse tagResponse = new TagResponse();

    List<TagInfo> tags = new ArrayList<>();

    TagInfo tag = new TagInfo();
    tag.setName("tag");
    tag.setWeight((float) 1);

    tags.add(tag);
    tagResponse.setTags(tags);
    return tagResponse;
  }
}
