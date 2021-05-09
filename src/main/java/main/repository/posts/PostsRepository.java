package main.repository.posts;

import main.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service("PostsRepository")
@Repository
public interface PostsRepository extends JpaRepository<Post, Integer> {

  @Query("SELECT p " +
      "FROM Post p " +
      "WHERE p.isActive = 1 " +
      "AND p.moderationStatus = 'ACCEPTED' " +
      "AND p.time <= CURRENT_DATE() " +
      "ORDER BY time DESC")
  Page<Post> findRecentPosts(Pageable pageable);

  @Query("SELECT p " +
      "FROM Post p " +
      "LEFT JOIN PostComment c " +
      "ON c.post.id = p.id " +
      "WHERE p.isActive = 1 " +
      "AND p.moderationStatus = 'ACCEPTED' " +
      "AND p.time <= CURRENT_DATE() " +
      "GROUP BY p.id " +
      "ORDER BY COUNT(*) DESC")
  Page<Post> findPopularPosts(Pageable pageable);

  @Query("SELECT p " +
      "FROM Post p " +
      "LEFT JOIN Vote v " +
      "ON v.post.id = p.id " +
      "WHERE p.isActive = 1 " +
      "AND p.moderationStatus = 'ACCEPTED' " +
      "AND p.time <= CURRENT_DATE() " +
      "GROUP BY p.id " +
      "ORDER BY " +
      "SUM(" +
          "CASE WHEN v.value IS NULL " +
          "THEN 0 " +
          "ELSE v.value " +
          "END" +
      ") DESC")
  Page<Post> findBestPosts(Pageable pageable);

  @Query("SELECT p " +
      "FROM Post p " +
      "WHERE p.isActive = 1 " +
      "AND p.moderationStatus = 'ACCEPTED' " +
      "AND p.time <= CURRENT_DATE() " +
      "ORDER BY time")
  Page<Post> findEarlyPosts(Pageable pageable);
}