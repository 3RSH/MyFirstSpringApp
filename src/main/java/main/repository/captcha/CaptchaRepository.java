package main.repository.captcha;

import java.sql.Timestamp;
import main.model.CaptchaCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("CaptchaRepository")
@Repository
public interface CaptchaRepository extends JpaRepository<CaptchaCode, Integer> {

  @Modifying
  @Transactional
  @Query("DELETE FROM CaptchaCode c WHERE c.time <= ?1")
  void deleteOldCaptcha(Timestamp timeThreshold);

  CaptchaCode findFirstBySecretCode(String secretCode);
}