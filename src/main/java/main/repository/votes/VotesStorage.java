package main.repository.votes;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service("VotesStorage")
public class VotesStorage implements VotesRepository {

  private final PostgresVotesRepository sqlVotesRepository;

  public VotesStorage(PostgresVotesRepository sqlVotesRepository) {
    this.sqlVotesRepository = sqlVotesRepository;
  }

  @Override
  public int getLikesCountByPostId(int postId) {
    AtomicInteger count = new AtomicInteger();

    sqlVotesRepository.findAll().forEach(vote -> {
      if (vote.getPost().getId() == postId && vote.getValue() == 1) count.getAndIncrement();
    });

    return count.get();
  }

  @Override
  public int getDislikeCountByPostId(int postId) {
    AtomicInteger count = new AtomicInteger();

    sqlVotesRepository.findAll().forEach(vote -> {
      if (vote.getPost().getId() == postId && vote.getValue() == -1) count.getAndIncrement();
    });

    return count.get();
  }
}
