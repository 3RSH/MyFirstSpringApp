package main.repository.posts;

import java.sql.Timestamp;
import java.util.List;
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
      "AND p.time <= CURRENT_TIMESTAMP() " +
      "ORDER BY time DESC")
  Page<Post> findRecentPosts(Pageable pageable);

  @Query("SELECT p " +
      "FROM Post p " +
      "LEFT JOIN PostComment c " +
      "ON c.post.id = p.id " +
      "WHERE p.isActive = 1 " +
      "AND p.moderationStatus = 'ACCEPTED' " +
      "AND p.time <= CURRENT_TIMESTAMP() " +
      "GROUP BY p.id " +
      "ORDER BY COUNT(*) DESC")
  Page<Post> findPopularPosts(Pageable pageable);

  @Query("SELECT p " +
      "FROM Post p " +
      "LEFT JOIN Vote v " +
      "ON v.post.id = p.id " +
      "WHERE p.isActive = 1 " +
      "AND p.moderationStatus = 'ACCEPTED' " +
      "AND p.time <= CURRENT_TIMESTAMP() " +
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
      "AND p.time <= CURRENT_TIMESTAMP() " +
      "ORDER BY time")
  Page<Post> findEarlyPosts(Pageable pageable);

  Page<Post> findAllByTitleContainingIgnoreCase(String query, Pageable pageable);

  @Query("SELECT p.time " +
      "FROM Post p " +
      "WHERE p.isActive = 1 " +
      "AND p.moderationStatus = 'ACCEPTED' " +
      "AND p.time <= CURRENT_TIMESTAMP() " +
      "ORDER BY time")
  List<Timestamp> getPublishTimes();

  @Query("SELECT p " +
      "FROM Post p " +
      "WHERE p.isActive = 1 " +
      "AND p.moderationStatus = 'ACCEPTED' " +
      "AND p.time <= CURRENT_TIMESTAMP() " +
      "AND EXTRACT(year FROM p.time) = ?1 " +
      "AND EXTRACT(month FROM p.time) = ?2 " +
      "AND EXTRACT(day FROM p.time) = ?3 " +
      "ORDER BY time DESC")
  Page<Post> findPostsByDate(int year, int month, int day, Pageable pageable);

  @Query("SELECT p " +
      "FROM Post p " +
      "LEFT JOIN TagBinding b " +
      "ON b.post.id = p.id " +
      "LEFT JOIN Tag t " +
      "ON t.id = b.tag.id " +
      "WHERE p.isActive = 1 " +
      "AND p.moderationStatus = 'ACCEPTED' " +
      "AND p.time <= CURRENT_TIMESTAMP() " +
      "AND t.name = ?1 " +
      "GROUP BY p.id " +
      "ORDER BY time DESC")
  Page<Post> findPostsByTag(String tag, Pageable pageable);

  @Query("SELECT p " +
      "FROM Post p " +
      "WHERE p.id = ?1")
  Post findPostsById(int id);

  @Query("SELECT p " +
      "FROM Post p " +
      "WHERE p.isActive = 0 " +
      "AND p.user.id = ?1 " +
      "ORDER BY p.id DESC")
  Page<Post> findInactivePostsByUser(int userId, Pageable pageable);

  @Query("SELECT p " +
      "FROM Post p " +
      "WHERE p.isActive = 1 " +
      "AND p.moderationStatus = 'NEW' " +
      "AND p.user.id = ?1 " +
      "ORDER BY p.id DESC")
  Page<Post> findPendingPostsByUser(int userId, Pageable pageable);

  @Query("SELECT p " +
      "FROM Post p " +
      "WHERE p.isActive = 1 " +
      "AND p.moderationStatus = 'DECLINED' " +
      "AND p.user.id = ?1 " +
      "ORDER BY p.id DESC")
  Page<Post> findDeclinedPostsByUser(int userId, Pageable pageable);

  @Query("SELECT p " +
      "FROM Post p " +
      "WHERE p.isActive = 1 " +
      "AND p.moderationStatus = 'ACCEPTED' " +
      "AND p.user.id = ?1 " +
      "ORDER BY p.id DESC")
  Page<Post> findPublishedPostsByUser(int userId, Pageable pageable);

  @Query("SELECT p " +
      "FROM Post p " +
      "WHERE p.isActive = 1 " +
      "AND p.moderationStatus = 'ACCEPTED' " +
      "AND p.user.id = ?1 " +
      "ORDER BY p.id DESC")
  List<Post> findPublishedPostsByUser(int userId);
}