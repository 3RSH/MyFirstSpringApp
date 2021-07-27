package main.repository.users;

import main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service("UsersRepository")
@Repository
public interface UsersRepository extends JpaRepository<User, Integer> {

  User findFirstByEmail(String email);

  User findFirstByCode(String code);
}