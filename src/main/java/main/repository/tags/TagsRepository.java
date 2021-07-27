package main.repository.tags;

import main.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("TagsRepository")
@Repository
public interface TagsRepository extends JpaRepository<Tag, Integer> {

  @Transactional
  @Modifying
  @Query("DELETE FROM Tag t " +
      "WHERE t.name = ?1")
  void deleteByName(String name);
}