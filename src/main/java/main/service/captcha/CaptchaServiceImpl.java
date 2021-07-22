package main.service.captcha;

import com.github.cage.Cage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import javax.imageio.ImageIO;
import main.api.response.CaptchaResponse;
import main.model.CaptchaCode;
import main.repository.captcha.CaptchaRepository;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CaptchaServiceImpl implements CaptchaService {

  private static final int CAPTCHA_WIDTH = 100;
  private static final int CAPTCHA_HEIGHT = 35;

  private final CaptchaRepository captchaRepository;

  @Value("${captcha.lifetimeBySec}")
  private long captchaLifetime;


  public CaptchaServiceImpl(
      @Qualifier("CaptchaRepository") CaptchaRepository captchaRepository) {
    this.captchaRepository = captchaRepository;
  }


  @Override
  public CaptchaResponse getNewCaptcha() {
    Timestamp timeThreshold = Timestamp.valueOf(LocalDateTime.now()
        .minusSeconds(captchaLifetime));

    captchaRepository.deleteOldCaptcha(timeThreshold);

    Cage cage = new Cage();
    String code = cage.getTokenGenerator().next();
    BufferedImage captchaImage = cage.drawImage(code);

    byte[] bytesOfImage = compressImageAndGetByteArray(captchaImage);
    String encodeImage = Base64.getEncoder().encodeToString(bytesOfImage);

    String secret = String.valueOf(code.hashCode());

    CaptchaCode captchaCode = new CaptchaCode();

    captchaCode.setCode(code);
    captchaCode.setSecretCode(secret);
    captchaCode.setTime(new Timestamp(System.currentTimeMillis()));

    captchaRepository.saveAndFlush(captchaCode);

    CaptchaResponse captchaResponse = new CaptchaResponse();

    captchaResponse.setImage("data:captchaImage/png;base64, " + encodeImage);
    captchaResponse.setSecret(secret);

    return captchaResponse;
  }

  private byte[] compressImageAndGetByteArray(BufferedImage image) {
    byte[] result = new byte[0];
    image = Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC,
        CAPTCHA_WIDTH, CAPTCHA_HEIGHT, Scalr.OP_ANTIALIAS);

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      ImageIO.write(image, "png", out);
      result = out.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return result;
  }
}