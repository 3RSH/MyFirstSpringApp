package main.service.user;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import main.api.RegisterErrors;
import main.api.response.EditProfileResponse;
import main.api.response.RegisterResponse;
import main.api.response.RestoreResponse;
import main.model.User;
import main.repository.captcha.CaptchaRepository;
import main.repository.users.UsersRepository;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

  @Override
  public ResponseEntity<?> editUser(Map<String, String> editRequest) {
    SecurityContext currentContext = SecurityContextHolder.getContext();
    User user = usersRepository.findFirstByEmail(
        currentContext.getAuthentication().getName());

    String name = editRequest.get("name");
    String email = editRequest.get("email");
    String password = editRequest.get("password");
    String removePhoto = editRequest.get("removePhoto");

    EditProfileResponse response = new EditProfileResponse();
    RegisterErrors errors = new RegisterErrors();

    response.setResult(isCorrectUserData(name, email, password, errors));

    if (!user.getEmail().equals(email) && usersRepository.findFirstByEmail(email) != null) {
      errors.setEmail("Этот e-mail уже зарегистрирован");
      response.setResult(false);
    }

    if (!response.isResult()) {
      response.setErrors(errors);

      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    if (!user.getName().equals(name)) {
      user.setName(name);
    }

    user.setEmail(email);

    if (password != null) {
      user.setPassword(passwordEncoder().encode(password));
    }

    if (removePhoto.equals("1")) {
      File file = new File("target/classes/static" +
          user.getPhoto().replaceAll("\\\\", "/"));

      try {
        FileUtils.deleteDirectory(file.getParentFile());
      } catch (IOException e) {
        e.printStackTrace();
      }

      user.setPhoto(null);

    } else {
      user.setPhoto(editRequest.get("photo"));
    }

    usersRepository.saveAndFlush(user);
    SecurityContextHolder.clearContext();

    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  @Override
  public RestoreResponse restoreUser(String email) {
    RestoreResponse response = new RestoreResponse();
    User user = usersRepository.findFirstByEmail(email);

    if (user == null) {
      return response;
    }

    String hash = getHash();
    user.setCode(hash);

    usersRepository.saveAndFlush(user);

    sendRestoreEmail(email, hash);
    response.setResult(true);

    return response;
  }


  private void sendRestoreEmail(String email, String hash) {
    String from = "b00b4@mail.ru";
    String host = "smtp.mail.ru";

    Properties properties = System.getProperties();
    properties.put("mail.smtp.auth", "true");
    properties.put("mail.smtp.starttls.enable", "true");
    properties.put("mail.smtp.host", host);
    properties.put("mail.smtp.port", "465");
    properties.put("mail.smtp.ssl.enable", "true");

    Session session = Session.getDefaultInstance(properties,
        new javax.mail.Authenticator() {
          @Override
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("b00b4", "OxAmbVkhTCNndp4iabbT");
          }
        });

    try {
      MimeMessage message = new MimeMessage(session);

      message.setFrom(new InternetAddress(from));
      message.addRecipient(RecipientType.TO, new InternetAddress(email));
      message.setSubject("Восстановление пароля");
      message.setText("Для смены пароля перейдите по ссылке:\n"
          + "<a href=\\\"localhost:8080//login/change-password/" + hash
          + "\\\">ССЫЛКА</a>");

      Transport.send(message);
    } catch (MessagingException e) {
      e.printStackTrace();
    }
  }

  private String getHash() {
    return String.valueOf(String.valueOf(Math.random()).hashCode());
  }

  private RegisterResponse getRegisterResponse(
      String password, String name, String email, String captcha, String captchaSecret) {

    RegisterResponse response = new RegisterResponse();
    RegisterErrors errors = new RegisterErrors();

    if (!captcha.equals(captchaRepository.findFirstBySecretCode(captchaSecret).getCode())) {
      errors.setCaptcha("Код с картинки введён неверно");
      response.setResult(false);
    }

    response.setResult(isCorrectUserData(name, email, password, errors));

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

  private boolean isCorrectUserData(
      String name, String email, String password, RegisterErrors errors) {

    boolean result = true;

    if (!name.matches(USERNAME_REGEX)) {
      errors.setName("Имя указано неверно");
      result = false;
    }

    if (!email.matches(EMAIL_REGEX)) {
      errors.setEmail("Почта указана неверно");
      result = false;
    }

    if (password != null && password.length() < 6) {
      errors.setPassword("Пароль короче 6-ти символов");
      result = false;
    }

    return result;
  }
}