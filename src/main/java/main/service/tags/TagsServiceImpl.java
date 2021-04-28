package main.service.tags;

import java.util.ArrayList;
import java.util.List;
import main.api.TagInfo;
import main.api.response.TagResponse;
import main.model.Post;
import main.model.Post.ModerationStatusType;
import main.model.Tag;
import main.model.TagBinding;
import main.repository.posts.PostsRepository;
import main.repository.tags.TagBindingsRepository;
import main.repository.tags.TagsRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TagsServiceImpl implements TagsService {

  private final TagsRepository tagsStorage;
  private final TagBindingsRepository tagBindingsStorage;
  private final PostsRepository postStorage;

  public TagsServiceImpl(@Qualifier("TagsStorage") TagsRepository tagsStorage,
      @Qualifier("TagBindingsStorage") TagBindingsRepository tagBindingsStorage,
      @Qualifier("PostsStorage")PostsRepository postStorage) {
    this.tagsStorage = tagsStorage;
    this.tagBindingsStorage = tagBindingsStorage;
    this.postStorage = postStorage;
  }


  @Override
  public TagResponse getTags() {

    int publicPostCount = 0;

    long postCount = postStorage.count();

    for (int i = 1; i <= postCount; i++) {

      Post post = postStorage.getPost(i);

      if (post.getIsActive() == 1
          && post.getModerationStatus() == ModerationStatusType.ACCEPTED
          && post.getTime().getTime() <= System.currentTimeMillis()) {
        publicPostCount++;
      }
    }

    List<Tag> tags = tagsStorage.getAllTags();
    List<TagBinding> tagBindings = tagBindingsStorage.getAllTagBindings();
    List<Float> weights = new ArrayList<>();

    for (Tag tag : tags) {
      int tagCount = 0;

      for (TagBinding tagBinding : tagBindings) {
        Post post = tagBinding.getPost();

        if (post.getIsActive() == 1
            && post.getModerationStatus() == ModerationStatusType.ACCEPTED
            && post.getTime().getTime() <= System.currentTimeMillis()) {
          if (tagBinding.getTag().equals(tag)) tagCount++;
        }

      }

      float dWeight = (float)tagCount/publicPostCount;
      weights.add(dWeight);
    }

    float maxDWeight = 0;

    for (float weight : weights) {
      if (maxDWeight < weight) maxDWeight = weight;
    }

    float k = 1/maxDWeight;

    for (int i = 0; i < weights.size(); i++) {
      float weight = weights.get(i) * k * 100;
      weight = (float)(int)((weight - (int) weight) >= 0.5F
          ? weight + 1
          : weight) / 100;

      weights.set(i, weight);
    }

    List<TagInfo> tagInfoList = new ArrayList<>();

    for (int i = 0; i < tags.size(); i++) {
      TagInfo tag = new TagInfo();
      tag.setName(tags.get(i).getName());
      tag.setWeight(weights.get(i));
      tagInfoList.add(tag);
    }

    TagResponse tagResponse = new TagResponse();
    tagResponse.setTags(tagInfoList);
    return tagResponse;
  }

  @Override
  public TagResponse getTag(String query) {
    int publicPostCount = 0;

    long postCount = postStorage.count();

    for (int i = 1; i <= postCount; i++) {

      Post post = postStorage.getPost(i);

      if (post.getIsActive() == 1
          && post.getModerationStatus() == ModerationStatusType.ACCEPTED
          && post.getTime().getTime() <= System.currentTimeMillis()) {
        publicPostCount++;
      }
    }


    List<Tag> tags = tagsStorage.getAllTags();
    List<TagBinding> tagBindings = tagBindingsStorage.getAllTagBindings();
    List<Float> weights = new ArrayList<>();

    for (Tag tag : tags) {
      int tagCount = 0;

      for (TagBinding tagBinding : tagBindings) {
        Post post = tagBinding.getPost();

        if (post.getIsActive() == 1
            && post.getModerationStatus() == ModerationStatusType.ACCEPTED
            && post.getTime().getTime() <= System.currentTimeMillis()) {
          if (tagBinding.getTag().equals(tag)) tagCount++;
        }

      }

      float dWeight = (float)tagCount/publicPostCount;
      weights.add(dWeight);
    }

    float maxDWeight = 0;

    for (float weight : weights) {
      if (maxDWeight < weight) maxDWeight = weight;
    }

    float k = 1/maxDWeight;

    for (float weight : weights) {
      weight = weight * k * 100;
      weight = (float)(int)((weight - (int) weight) >= 0.5F
          ? weight + 1
          : weight) / 100;
    }

    List<TagInfo> tagInfoList = new ArrayList<>();

    for (int i = 0; i < tags.size(); i++) {
      TagInfo tag = new TagInfo();

      if (tags.get(i).getName().matches(query + ".*")) {
        tag.setName(tags.get(i).getName());
        tag.setWeight(weights.get(i));
        tagInfoList.add(tag);
      }
    }

    TagResponse tagResponse = new TagResponse();
    tagResponse.setTags(tagInfoList);
    return tagResponse;

  }
}
