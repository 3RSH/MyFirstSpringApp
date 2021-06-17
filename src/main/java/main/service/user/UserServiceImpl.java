package main.service.user;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import main.api.RegisterErrors;
import main.api.response.RegisterResponse;
import main.model.User;
import main.repository.captcha.CaptchaRepository;
import main.repository.users.UsersRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  private static final String USERNAME_REGEX = "[A-Za-zА-Яа-яё0-9\\-]{2,20}\\s*"
      + "[A-Za-zА-Яа-яё0-9\\-]{0,20}\\s*[A-Za-zА-Яа-яё0-9\\-]{0,20}";
  private static final String EMAIL_REGEX = "^([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]"
      + "+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$";

  private final UsersRepository usersRepository;
  private final CaptchaRepository captchaRepository;


  public UserServiceImpl(@Qualifier("UsersRepository") UsersRepository usersRepository,
      @Qualifier("CaptchaRepository") CaptchaRepository captchaRepository) {
    this.usersRepository = usersRepository;
    this.captchaRepository = captchaRepository;
  }


  @Override
  public synchronized RegisterResponse addUser(Map<String, String> registerRequest) {
    String password = registerRequest.get("password");
    String name = registerRequest.get("name");
    String email = registerRequest.get("e_mail");
    String captcha = registerRequest.get("captcha");
    String captchaSecret = registerRequest.get("captcha_secret");

    RegisterResponse response
        = getRegisterResponse(password, name, email, captcha, captchaSecret);

    if (!response.isResult()) {
      return response;
    }

    User user = new User();

    user.setIsModerator((short) -1);
    user.setRegTime(Timestamp.valueOf(LocalDateTime.now()));
    user.setName(registerRequest.get("name"));
    user.setEmail(registerRequest.get("e_mail"));
    user.setPassword(passwordEncoder().encode(registerRequest.get("password")));

    usersRepository.saveAndFlush(user);

    return response;
  }

  private RegisterResponse getRegisterResponse(
      String password, String name, String email, String captcha, String captchaSecret) {

    RegisterResponse response = new RegisterResponse();
    RegisterErrors errors = new RegisterErrors();

    if (!captcha.equals(captchaRepository.findFirstBySecretCode(captchaSecret).getCode())) {
      errors.setCaptcha("Код с картинки введён неверно");
      response.setResult(false);

    }

    if (password.length() < 6) {
      errors.setPassword("Пароль короче 6-ти символов");
      response.setResult(false);
    }

    if (!name.matches(USERNAME_REGEX)) {
      errors.setName("Имя указано неверно");
      response.setResult(false);
    }

    if (!email.matches(EMAIL_REGEX)) {
      errors.setEmail("Почта указана неверно");
      response.setResult(false);
    }

    if (usersRepository.findFirstByEmail(email) != null) {
      errors.setEmail("Этот e-mail уже зарегистрирован");
      response.setResult(false);
    }

    if (!response.isResult()) {
      response.setErrors(errors);
    }

    return response;
  }

  private PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}