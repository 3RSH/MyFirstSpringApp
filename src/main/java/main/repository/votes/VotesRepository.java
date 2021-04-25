package main.repository.votes;

public interface VotesRepository {

  int getLikesCountByPostId(int postId);
  int getDislikeCountByPostId(int postId);
}