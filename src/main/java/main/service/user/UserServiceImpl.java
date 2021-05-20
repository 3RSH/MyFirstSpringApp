package main.service.user;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import main.api.RegisterErrors;
import main.api.response.FailRegisterResponse;
import main.api.response.RegisterResponse;
import main.model.User;
import main.repository.captcha.CaptchaRepository;
import main.repository.users.UsersRepository;
import org.springframework.beans.factory.annotation.Qualifier;
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
  public RegisterResponse addUser(Map<String, String> registerRequest) {
    String password = registerRequest.get("password");
    String name = registerRequest.get("name");
    String email = registerRequest.get("e_mail");
    String captcha = registerRequest.get("captcha");
    String captchaSecret = registerRequest.get("captcha_secret");

    FailRegisterResponse errorResponse
        = checkRegisterRequest(password, name, email, captcha, captchaSecret);

    if (!errorResponse.isResult()) {
      return errorResponse;
    }

    User user = new User();

    user.setIsModerator((short) -1);
    user.setRegTime(Timestamp.valueOf(LocalDateTime.now()));
    user.setName(registerRequest.get("name"));
    user.setEmail(registerRequest.get("e_mail"));
    user.setPassword(registerRequest.get("password"));

    usersRepository.saveAndFlush(user);

    return new RegisterResponse();
  }

  private FailRegisterResponse checkRegisterRequest(
      String password, String name, String email, String captcha, String captchaSecret) {

    FailRegisterResponse errorResponse = new FailRegisterResponse();
    RegisterErrors errors = new RegisterErrors();

    if (!captcha.equals(captchaRepository.findFirstBySecretCode(captchaSecret).getCode())) {
      errors.setCaptcha("Код с картинки введён неверно");
      errorResponse.setResult(false);

    }

    if (password.length() < 6) {
      errors.setPassword("Пароль короче 6-ти символов");
      errorResponse.setResult(false);
    }

    if (!name.matches(USERNAME_REGEX)) {
      errors.setName("Имя указано неверно");
      errorResponse.setResult(false);
    }

    if (!email.matches(EMAIL_REGEX)) {
      errors.setEmail("Почта указана неверно");
      errorResponse.setResult(false);
    }

    if (usersRepository.findFirstByEmail(email) != null) {
      errors.setEmail("Этот e-mail уже зарегистрирован");
      errorResponse.setResult(false);
    }

    errorResponse.setErrors(errors);

    return errorResponse;
  }
}