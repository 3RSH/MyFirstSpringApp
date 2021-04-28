package main.repository.votes;

import main.model.Vote;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostgresVotesRepository extends CrudRepository<Vote, Integer> {

}