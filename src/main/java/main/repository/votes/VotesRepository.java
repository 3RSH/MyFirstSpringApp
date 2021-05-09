package main.repository.votes;

import main.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service("VotesRepository")
@Repository
public interface VotesRepository extends JpaRepository<Vote, Integer> {

}